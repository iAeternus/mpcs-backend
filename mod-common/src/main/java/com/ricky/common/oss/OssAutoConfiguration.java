package com.ricky.common.oss;

import com.ricky.common.oss.minio.MinioOssService;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OssAutoConfiguration {

    @Bean
    public OssService ossService(MinioClient minioClient) {
        return new MinioOssService(minioClient);
    }

}
