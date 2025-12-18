package com.ricky.testsuite;

import com.ricky.MpcsBackendApplication;
import com.ricky.common.context.ThreadLocalContext;
import com.ricky.common.context.UserContext;
import com.ricky.common.domain.marker.Response;
import com.ricky.common.event.publish.PublishingDomainEventDao;
import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyError;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.password.IPasswordEncoder;
import com.ricky.file.infra.FileRepository;
import com.ricky.file.infra.impl.GridFsFileStorage;
import com.ricky.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.ErrorResponse;
import org.springframework.web.context.WebApplicationContext;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MpcsBackendApplication.class)
public class BaseApiTest {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected SetUpService setUpService;

    @Autowired
    protected TearDownService tearDownService;

    @Autowired
    protected PublishingDomainEventDao publishingDomainEventDao;

    @Autowired
    protected IPasswordEncoder passwordEncoder;

    @Autowired
    protected FileRepository fileRepository;

    @Autowired
    protected GridFsFileStorage gridFsFileStorage;

    @Autowired
    protected FileHasherFactory fileHasherFactory;

    // add here...

    @BeforeEach
    protected void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ThreadLocalContext.setContext(UserContext.of(User.newUserId(), "test_username"));
    }

    @AfterEach
    protected void destroy() {
        ThreadLocalContext.removeContext();
    }

//    public static void assertErrorCode(Supplier<Response> apiCall, ErrorCodeEnum expectedCode) {
//        MyError error = apiCall.get().then().statusCode(expectedCode.getStatus()).extract().as(MyError.class);
//        assertEquals(expectedCode, error.getCode());
//    }

}
