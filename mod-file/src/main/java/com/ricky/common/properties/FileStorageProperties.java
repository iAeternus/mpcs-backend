package com.ricky.common.properties;

import com.ricky.common.hash.HashAlgorithm;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpcs.file")
public class FileStorageProperties {

    private HashConfig hash;

    @Data
    public static class HashConfig {
        private HashAlgorithm algorithm;
    }

}
