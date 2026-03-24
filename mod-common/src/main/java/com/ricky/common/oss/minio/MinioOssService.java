package com.ricky.common.oss.minio;

import com.ricky.common.oss.OssService;
import io.minio.*;
import io.minio.messages.Part;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
public class MinioOssService implements OssService {

    private final MinioClient minioClient;

    @Override
    public void ensureBucket(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO ensureBucket failed", e);
        }
    }

    @Override
    public void putObject(String bucket, String objectKey, InputStream input, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(input, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO upload failed", e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO getObject failed", e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey, long offset, long length) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .offset(offset)
                            .length(length)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO getObject with range failed", e);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("MinIO delete failed", e);
        }
    }

    @Override
    public boolean exists(String bucket, String objectKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String initiateMultipartUpload(String bucket, String objectKey) {
        try {
            ExtendedMinioClient client = (ExtendedMinioClient) minioClient;
            return client.initMultiPartUpload(bucket, null, objectKey);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO initiateMultipartUpload failed", e);
        }
    }

    @Override
    public String uploadPart(String bucket, String objectKey, String uploadId, int partNumber, InputStream input, long size) {
        try {
            ExtendedMinioClient client = (ExtendedMinioClient) minioClient;
            return client.uploadPart(bucket, null, objectKey, uploadId, partNumber, input, size);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO uploadPart failed", e);
        }
    }

    @Override
    public void completeMultipartUpload(String bucket, String objectKey, String uploadId, java.util.List<PartETag> parts) {
        try {
            ExtendedMinioClient client = (ExtendedMinioClient) minioClient;
            Part[] partArray = parts.stream()
                    .map(p -> new Part(p.partNumber(), p.eTag()))
                    .toArray(Part[]::new);
            client.mergeMultipartUpload(bucket, null, objectKey, uploadId, partArray);
        } catch (Exception e) {
            throw new IllegalStateException("MinIO completeMultipartUpload failed", e);
        }
    }

    @Override
    public void abortMultipartUpload(String bucket, String objectKey, String uploadId) {
        // Not implemented for minio 8.3.3
    }

    @Override
    public long getObjectSize(String bucket, String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            return stat.size();
        } catch (Exception e) {
            throw new IllegalStateException("MinIO getObjectSize failed", e);
        }
    }
}
