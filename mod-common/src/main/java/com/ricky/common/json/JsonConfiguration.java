package com.ricky.common.json;

import com.ricky.common.utils.MyObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {

    @Bean
    public MyObjectMapper objectMapper() {
        return new MyObjectMapper();
    }

}
