package com.ricky.common.startup;

import com.ricky.common.properties.RedisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@AutoConfiguration
public class StartupAutoConfiguration {

    @Bean
    public RedisStreamInitializer redisStreamInitializer(RedisTemplate<String, Object> redisTemplate, RedisProperties redisProperties) {
        return new RedisStreamInitializer(redisTemplate, redisProperties);
    }

}
