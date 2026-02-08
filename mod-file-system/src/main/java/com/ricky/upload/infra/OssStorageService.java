package com.ricky.upload.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.common.oss.OssService;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.MimeTypeResolver;
import com.ricky.file.domain.storage.OssStorageId;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.UploadSession;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

import static com.ricky.common.constants.ConfigConstants.FILE_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.MERGE_CHUNKS_FAILED;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;

@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mpcs.config", name = "storage", havingValue = "oss")
public class OssStorageService implements StorageService {

    private final OssService ossService;
    private final FileHasherFactory fileHasherFactory;
    private final MimeTypeResolver mimeTypeResolver;

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

        try (InputStream mergedStream = new SequenceInputStream(Collections.enumeration(buildChunkStreams(session, chunkDir, digest)))) {
            // 一次性流式上传
            ossService.putObject(
                    FILE_BUCKET,
                    objectKey,
                    mergedStream,
                    session.getTotalSize(),
                    contentType
            );

            byte[] hashBytes = digest.digest();
            String hash = String.valueOf(Hex.encode(hashBytes));

            return StoredFile.builder()
                    .storageId(OssStorageId.withFileBucket(objectKey))
                    .hash(hash)
                    .size(session.getTotalSize())
                    .build();
        } catch (IOException e) {
            throw new MyException(MERGE_CHUNKS_FAILED, "Merge chunks and upload to OSS failed",
                    "uploadSessionId", session.getId());
        } finally {
            // 清理分片
            cleanupChunks(session, chunkDir);
        }
    }

    private List<InputStream> buildChunkStreams(UploadSession session, Path chunkDir, MessageDigest digest) throws IOException {
        List<InputStream> streams = new java.util.ArrayList<>(session.getTotalChunks());

        for (int i = 0; i < session.getTotalChunks(); i++) {
            Path chunk = chunkDir.resolve(String.valueOf(i));

            InputStream in = Files.newInputStream(chunk);
            streams.add(new DigestInputStream(in, digest));
        }

        return streams;
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
}
