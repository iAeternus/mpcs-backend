package com.ricky.common.oss;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class MinioOssService implements OssService {

    // private final MinioClient minioClient;

    @Override
    public void putObject(String bucket, String objectKey, InputStream input, long size, String contentType) {

    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        return null;
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {

    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        return false;
    }
}
