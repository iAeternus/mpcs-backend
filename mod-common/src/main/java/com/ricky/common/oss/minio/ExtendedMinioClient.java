package com.ricky.common.oss.minio;

import com.google.common.collect.Multimap;
import io.minio.*;
import io.minio.messages.Part;
import lombok.SneakyThrows;

import java.io.InputStream;

public class ExtendedMinioClient extends MinioClient {

    public ExtendedMinioClient(MinioClient client) {
        super(client);
    }

    @SneakyThrows
    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String bucketName, String region, String objectName,
                                                                 Multimap<String, String> headers, Multimap<String, String> extraQueryParams) {
        return super.createMultipartUpload(bucketName, region, objectName, headers, extraQueryParams);
    }

    @SneakyThrows
    @Override
    public ObjectWriteResponse completeMultipartUpload(String bucketName, String region, String objectName,
                                                        String uploadId, Part[] parts,
                                                        Multimap<String, String> extraHeaders, Multimap<String, String> extraQueryParams) {
        return super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, extraHeaders, extraQueryParams);
    }

    @SneakyThrows
    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(String bucketName, String region, String objectName,
                                                               String uploadId, Multimap<String, String> headers, 
                                                               Multimap<String, String> extraQueryParams) {
        return super.abortMultipartUpload(bucketName, region, objectName, uploadId, headers, extraQueryParams);
    }

    public String initMultiPartUpload(String bucket, String region, String object) throws Exception {
        CreateMultipartUploadResponse response = super.createMultipartUpload(bucket, region, object, null, null);
        return response.result().uploadId();
    }

    public String uploadPart(String bucketName, String region, String objectName, String uploadId,
                             int partNumber, InputStream data, long length) throws Exception {
        UploadPartResponse response = super.uploadPart(bucketName, region, objectName, data, length, uploadId, partNumber, null, null);
        return response.etag();
    }

    public void mergeMultipartUpload(String bucketName, String region, String objectName,
                                       String uploadId, Part[] parts) throws Exception {
        super.completeMultipartUpload(bucketName, region, objectName, uploadId, parts, null, null);
    }
}