package com.ricky.upload.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.oss.OssService;
import com.ricky.common.oss.OssService.PartETag;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.MimeTypeResolver;
import com.ricky.file.domain.storage.OssStorageId;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.MERGE_CHUNKS_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mpcs.config", name = "storage", havingValue = "oss")
public class OssStorageService implements StorageService {

    private final OssService ossService;
    private final FileHasherFactory fileHasherFactory;
    private final MimeTypeResolver mimeTypeResolver;
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
    public StoredFile mergeChunksAndStore(UploadSession session, Path chunkDir) {
        String filename = session.getFilename();
        String objectKey = generateObjectKey(filename);

        FileExtension extension = FileExtension.fromFilename(session.getFilename());
        String contentType = mimeTypeResolver.resolve(extension);

        MessageDigest digest = fileHasherFactory.getFileHasher().newDigest();
        List<InputStream> streams = new java.util.ArrayList<>(session.getTotalChunks());

        try {
            for (int i = 0; i < session.getTotalChunks(); i++) {
                Path chunk = chunkDir.resolve(String.valueOf(i));
                InputStream in = Files.newInputStream(chunk);
                streams.add(new DigestInputStream(in, digest));
            }

            try (InputStream mergedStream = new SequenceInputStream(Collections.enumeration(streams))) {
                ossService.putObject(FILE_BUCKET, objectKey, mergedStream, session.getTotalSize(), contentType);
            }

            byte[] hashBytes = digest.digest();
            String hash = bytesToHex(hashBytes);

            log.info("Merged {} chunks to OSS: objectKey={}", session.getTotalChunks(), objectKey);

            return StoredFile.builder()
                    .storageId(OssStorageId.withFileBucket(objectKey))
                    .hash(hash)
                    .size(session.getTotalSize())
                    .build();
        } catch (IOException e) {
            throw new MyException(MERGE_CHUNKS_FAILED, "Merge chunks and upload to OSS failed",
                    "uploadSessionId", session.getId());
        } finally {
            cleanupChunks(session, chunkDir);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void cleanupChunks(UploadSession session, Path chunkDir) {
        for (int i = 0; i < session.getTotalChunks(); i++) {
            try {
                Files.deleteIfExists(chunkDir.resolve(String.valueOf(i)));
            } catch (IOException ignored) {
            }
        }
        try {
            Files.deleteIfExists(chunkDir);
        } catch (IOException ignored) {
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
    public StoredFile completeMultipartUpload(String uploadId, String filename, long totalSize) {
        MultipartUploadContext context = multipartContexts.remove(uploadId);
        if (context == null) {
            throw new MyException(OSS_ERROR, "Upload session not found", "uploadId", uploadId);
        }

        context.parts.sort((a, b) -> Integer.compare(a.partNumber(), b.partNumber()));
        ossService.completeMultipartUpload(FILE_BUCKET, context.objectKey, uploadId, context.parts);

        String hash = calculateHashFromOSS(context.objectKey);
        log.info("Completed multipart upload: uploadId={}, objectKey={}, hash={}", uploadId, context.objectKey, hash);

        return StoredFile.builder()
                .storageId(OssStorageId.withFileBucket(context.objectKey))
                .hash(hash)
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

    private String calculateHashFromOSS(String objectKey) {
        try (InputStream is = ossService.getObject(FILE_BUCKET, objectKey)) {
            MessageDigest digest = fileHasherFactory.getFileHasher().newDigest();
            try (DigestInputStream dis = new DigestInputStream(is, digest)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // consume stream
                }
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new MyException(OSS_ERROR, "Calculate hash from OSS failed", "objectKey", objectKey, "exception", e);
        }
    }

    @Override
    public InputStream getFileStream(StorageId storageId) {
        OssStorageId ossStorageId = (OssStorageId) storageId;
        return ossService.getObject(ossStorageId.getBucket(), ossStorageId.getObjectKey());
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

    private static class SequenceInputStream extends java.io.SequenceInputStream {
        public SequenceInputStream(java.util.Enumeration<? extends InputStream> e) {
            super(e);
        }
    }
}
