package com.ricky.common.json;

import com.ricky.common.utils.MyObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class JsonAutoConfiguration {

    @Bean
    public MyObjectMapper myObjectMapper() {
        return new MyObjectMapper();
    }

}
