package com.ricky.common.oss.minio;

import lombok.Data;

@Data
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;

}
