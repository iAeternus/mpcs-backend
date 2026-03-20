package com.ricky.upload.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.oss.OssService;
import com.ricky.common.oss.OssService.PartETag;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.storage.OssStorageId;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.upload.domain.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mpcs.config", name = "storage", havingValue = "oss")
public class OssStorageService implements StorageService {

    private final OssService ossService;
    private final Map<String, MultipartUploadContext> multipartContexts = new ConcurrentHashMap<>();

    private static class MultipartUploadContext {
        final String objectKey;
        final List<PartETag> parts;
        final int expectedParts;

        MultipartUploadContext(String objectKey, int expectedParts) {
            this.objectKey = objectKey;
            this.expectedParts = expectedParts;
            this.parts = new java.util.ArrayList<>();
        }
    }

    @Override
    public StorageId store(MultipartFile multipartFile) {
        try {
            String objectKey = generateObjectKey(multipartFile.getOriginalFilename());

            ossService.putObject(
                    FILE_BUCKET,
                    objectKey,
                    multipartFile.getInputStream(),
                    multipartFile.getSize(),
                    multipartFile.getContentType()
            );

            return OssStorageId.withFileBucket(objectKey);
        } catch (IOException ex) {
            throw new MyException(OSS_ERROR, "OSS上传失败", "exception", ex);
        }
    }

    @Override
    public String initMultipartUpload(String filename) {
        String objectKey = generateObjectKey(filename);
        String uploadId = ossService.initiateMultipartUpload(FILE_BUCKET, objectKey);
        multipartContexts.put(uploadId, new MultipartUploadContext(objectKey, -1));
        log.info("Initialized multipart upload: uploadId={}, objectKey={}", uploadId, objectKey);
        return uploadId;
    }

    @Override
    public String uploadPart(String uploadId, int partNumber, MultipartFile chunk) {
        MultipartUploadContext context = multipartContexts.get(uploadId);
        if (context == null) {
            throw new MyException(OSS_ERROR, "Upload session not found", "uploadId", uploadId);
        }

        try {
            String etag = ossService.uploadPart(FILE_BUCKET, context.objectKey, uploadId,
                    partNumber, chunk.getInputStream(), chunk.getSize());
            context.parts.add(new PartETag(etag, partNumber));
            log.debug("Uploaded part: uploadId={}, partNumber={}, etag={}", uploadId, partNumber, etag);
            return etag;
        } catch (IOException e) {
            throw new MyException(OSS_ERROR, "Upload part failed", "uploadId", uploadId, "partNumber", partNumber, "exception", e);
        }
    }

    @Override
    public StoredFile completeMultipartUpload(String uploadId, String filename, long totalSize, String expectedHash) {
        MultipartUploadContext context = multipartContexts.remove(uploadId);
        if (context == null) {
            throw new MyException(OSS_ERROR, "Upload session not found", "uploadId", uploadId);
        }

        context.parts.sort((a, b) -> Integer.compare(a.partNumber(), b.partNumber()));
        ossService.completeMultipartUpload(FILE_BUCKET, context.objectKey, uploadId, context.parts);

        log.info("Completed multipart upload: uploadId={}, objectKey={}", uploadId, context.objectKey);

        return StoredFile.builder()
                .storageId(OssStorageId.withFileBucket(context.objectKey))
                .hash(expectedHash)
                .size(totalSize)
                .build();
    }

    @Override
    public void abortMultipartUpload(String uploadId) {
        MultipartUploadContext context = multipartContexts.remove(uploadId);
        if (context != null) {
            ossService.abortMultipartUpload(FILE_BUCKET, context.objectKey, uploadId);
            log.info("Aborted multipart upload: uploadId={}", uploadId);
        }
    }

    @Override
    public InputStream getFileStream(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.getObject(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    @Override
    public InputStream getFileStream(StorageId storageId, long offset, long length) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.getObject(ossStorageId.getBucket(), ossStorageId.getObjectKey(), offset, length);
    }

    @Override
    public void delete(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        ossService.deleteObject(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    @Override
    public void delete(List<StorageId> storageIds) {
        storageIds.forEach(this::delete);
    }

    @Override
    public boolean exists(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.exists(ossStorageId.getBucket(), ossStorageId.getObjectKey());
    }

    private String generateObjectKey(String filename) {
        return UuidGenerator.newShortUuid() + "/" + filename;
    }
}
