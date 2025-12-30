package com.ricky.common.properties;

import com.ricky.common.hash.HashAlgorithm;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mpcs.file")
public class FileProperties {

    private HashConfig hash;
    private FileUploadConfig upload;

    @Data
    public static class HashConfig {
        private HashAlgorithm algorithm; // 文件hash算法
    }

    @Data
    public static class FileUploadConfig {

        /**
         * 存放分片的路径
         */
        private String chunkDir;

        /**
         * 分片大小，单位：byte
         */
        @Min(8192)
        private Integer chunkSize = 1024 * 1024;

        /**
         * 缓冲区大小，单位：byte
         */
        @Min(8192)
        private Integer bufferSize = 1024 * 1024;

        /**
         * 并行线程数
         */
        @NotNull
        @Min(1)
        @Max(32)
        private Integer parallelism = Math.max(2, Runtime.getRuntime().availableProcessors());

    }

}
