package com.ricky.common.restclient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import static java.time.Duration.ofSeconds;

@AutoConfiguration
public class RestTemplateAutoConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(ofSeconds(10))
                .readTimeout(ofSeconds(10))
                .build();
    }
}
