package com.ricky.common.properties;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class PropertiesAutoConfiguration {

    @Bean
    @ConfigurationProperties("mpcs.jwt")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    @ConfigurationProperties("mpcs.redis")
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    @ConfigurationProperties("mpcs.config")
    public SystemProperties systemProperties() {
        return new SystemProperties();
    }

}
