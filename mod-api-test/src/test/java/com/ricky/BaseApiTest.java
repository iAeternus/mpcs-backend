package com.ricky;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.DomainEventType;
import com.ricky.common.event.consume.ConsumingDomainEventDao;
import com.ricky.common.event.publish.PublishingDomainEvent;
import com.ricky.common.event.publish.PublishingDomainEventDao;
import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.ErrorResponse;
import com.ricky.common.exception.MyError;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.json.JsonCodec;
import com.ricky.common.password.IPasswordEncoder;
import com.ricky.common.properties.FileProperties;
import com.ricky.common.properties.SystemProperties;
import com.ricky.common.security.jwt.JwtService;
import com.ricky.file.domain.FileRepository;
import com.ricky.fileextra.domain.FileExtraRepository;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import com.ricky.group.domain.GroupRepository;
import com.ricky.publicfile.domain.PublicFileRepository;
import com.ricky.upload.domain.StorageService;
import com.ricky.user.domain.UserRepository;
import com.ricky.verification.domain.VerificationCodeRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.function.Supplier;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.ricky.common.constants.ConfigConstants.AUTHORIZATION;
import static com.ricky.common.constants.ConfigConstants.AUTH_COOKIE_NAME;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@SuppressWarnings({"unchecked"})
@ActiveProfiles("ci")
@Execution(CONCURRENT)
@SpringBootTest(classes = MpcsBackendApplication.class, webEnvironment = RANDOM_PORT)
public abstract class BaseApiTest {

    @Autowired
    protected SystemProperties systemProperties;

    @Autowired
    protected FileProperties fileProperties;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JsonCodec jsonCodec;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected SetupApi setupApi;

    @Autowired
    protected PublishingDomainEventDao publishingDomainEventDao;

    @Autowired
    protected ConsumingDomainEventDao<DomainEvent> consumingDomainEventDao;

    @Autowired
    protected FileHasherFactory fileHasherFactory;

    @Autowired
    protected StorageService storageService;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected IPasswordEncoder passwordEncoder;

    @Autowired
    protected FolderHierarchyDomainService folderHierarchyDomainService;

    @Autowired
    protected FileRepository fileRepository;

    @Autowired
    protected VerificationCodeRepository verificationCodeRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected FolderHierarchyRepository folderHierarchyRepository;

    @Autowired
    protected FileExtraRepository fileExtraRepository;

    @Autowired
    protected GroupRepository groupRepository;

    @Autowired
    protected PublicFileRepository publicFileRepository;

    @LocalServerPort
    protected int port;

    // 手动注入，应为given应该保持静态
    private static ObjectMapper STATIC_OBJECT_MAPPER;

    @Autowired
    void setObjectMapper(ObjectMapper objectMapper) {
        STATIC_OBJECT_MAPPER = objectMapper;
    }

    public static RequestSpecification given() {
        return RestAssured.given()
                .config(config()
                        .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                                (type, s) -> STATIC_OBJECT_MAPPER))
                        .encoderConfig(new EncoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))
                        .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()));
    }

    public static RequestSpecification given(String jwt) {
        if (isNotBlank(jwt)) {
            return given().cookie(AUTH_COOKIE_NAME, jwt);
        }

        return given();
    }

    public static RequestSpecification givenBearer(String jwt) {
        if (isNotBlank(jwt)) {
            return given().header(AUTHORIZATION, String.format("Bearer %s", jwt));
        }

        return given();
    }

    public static RequestSpecification givenBasic(String username, String password) {
        return given().auth().preemptive().basic(username, password);
    }

    @BeforeEach
    public void setUp() {
        objectMapper.enable(INDENT_OUTPUT);
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1.0";
//        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(JSON)
                .setAccept(JSON)
                .build();
    }

    @AfterEach
    public void cleanUp() {
    }

    public static void assertError(Supplier<Response> apiCall, ErrorCodeEnum expectedCode) {
        MyError error = apiCall.get().then().statusCode(expectedCode.getStatus()).extract().as(ErrorResponse.class).getError();
        assertEquals(expectedCode, error.getCode());
    }

    protected <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        Query query = query(where(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.arId).is(arId)
                .and(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.type).is(type))
                .with(by(DESC, PublishingDomainEvent.Fields.raisedAt));
        PublishingDomainEvent publishingDomainEvent = mongoTemplate.findOne(query, PublishingDomainEvent.class);
        if (publishingDomainEvent == null) {
            return null;
        }
        return (T) publishingDomainEvent.getEvent();
    }
}
