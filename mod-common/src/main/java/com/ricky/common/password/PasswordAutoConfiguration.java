package com.ricky.common.password;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/11
 * @className PasswordConfiguration
 * @desc
 */
@AutoConfiguration
public class PasswordAutoConfiguration {

    @Bean
    public IPasswordEncoder passwordEncoder() {
        return new SHAPasswordEncoder();
    }

}
