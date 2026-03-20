package com.ricky.common.oss;

import java.io.InputStream;
import java.util.List;

public interface OssService {

    void ensureBucket(String bucket);

    void putObject(String bucket, String objectKey, InputStream input, long size, String contentType);

    InputStream getObject(String bucket, String objectKey);

    InputStream getObject(String bucket, String objectKey, long offset, long length);

    void deleteObject(String bucket, String objectKey);

    boolean exists(String bucket, String objectKey);

    String initiateMultipartUpload(String bucket, String objectKey);

    String uploadPart(String bucket, String objectKey, String uploadId, int partNumber, InputStream input, long size);

    void completeMultipartUpload(String bucket, String objectKey, String uploadId, List<PartETag> parts);

    void abortMultipartUpload(String bucket, String objectKey, String uploadId);

    record PartETag(String eTag, int partNumber) {
    }
}
