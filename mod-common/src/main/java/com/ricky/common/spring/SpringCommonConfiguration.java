package com.ricky.common.spring;

import com.ricky.common.utils.MyObjectMapper;
import io.lettuce.core.tracing.TraceContext;
import io.lettuce.core.tracing.Tracer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className SpringCommonConfiguration
 * @desc Spring通用配置类
 */
@EnableCaching
@EnableAsync
@EnableRetry
@Configuration
public class SpringCommonConfiguration {

//    // TODO mock tracer
//    @Bean
//    public Tracer tracer() {
//        return new Tracer() {
//            @Override
//            public Span nextSpan() {
//                return null;
//            }
//
//            @Override
//            public Span nextSpan(TraceContext traceContext) {
//                return null;
//            }
//        };
//    }

}
