package com.ricky.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.ricky.common.event.DomainEventSubtypeRegistrar;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.ricky.common.constants.ConfigConstants.CHINA_TIME_ZONE;
import static com.ricky.common.utils.ValidationUtils.nullIfBlank;
import static java.time.ZoneId.of;
import static java.util.TimeZone.getTimeZone;

@AutoConfiguration
public class JsonAutoConfiguration {

    private final ObjectMapper objectMapper;

    @Autowired
    public JsonAutoConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void configureObjectMapper() {
        configure(objectMapper);

        objectMapper.addMixIn(
                org.springframework.ai.openai.OpenAiChatOptions.class,
                OpenAiChatOptionsMixin.class
        );
    }

    @Bean
    public JsonCodec jsonCodec(ObjectMapper objectMapper) {
        return new JsonCodec(objectMapper);
    }

    @Bean
    public ObjectMapper domainEventSubtypeRegistrarInvoker(ObjectMapper objectMapper, ObjectProvider<DomainEventSubtypeRegistrar> registrars) {
        registrars.orderedStream()
                .forEach(r -> r.register(objectMapper));
        return objectMapper;
    }

    /**
     * 配置 ObjectMapper
     *
     * @param objectMapper ObjectMapper 实例
     */
    private static void configure(ObjectMapper objectMapper) {
        objectMapper.findAndRegisterModules()
                .setTimeZone(getTimeZone(of(CHINA_TIME_ZONE)))
                .setVisibility(ALL, NONE)
                .setVisibility(FIELD, ANY)
                .registerModule(new GuavaModule())
                .registerModule(instantModule())
                .registerModule(trimStringModule())
                .configure(WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 序列化和反序列化 Instant 类型
     */
    private static SimpleModule instantModule() {
        return new SimpleModule()
                .addSerializer(Instant.class, instantSerializer())
                .addDeserializer(Instant.class, instantDeserializer());
    }

    /**
     * 在反序列化时修剪字符串
     */
    private static SimpleModule trimStringModule() {
        return new SimpleModule()
                .addDeserializer(String.class, new StdScalarDeserializer<>(String.class) {
                    @Override
                    public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
                        return nullIfBlank(jsonParser.getValueAsString().trim());
                    }
                });
    }

    private static JsonDeserializer<Instant> instantDeserializer() {
        return new JsonDeserializer<>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext d) throws IOException {
                return Instant.ofEpochMilli(p.getValueAsLong());
            }
        };
    }

    private static JsonSerializer<Instant> instantSerializer() {
        return new JsonSerializer<>() {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.toEpochMilli());
            }
        };
    }

}
