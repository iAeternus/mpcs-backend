package com.ricky.upload.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.UPLOAD_SESSION_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.UPLOAD_SESSION_ID_PREFIX;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Getter
@TypeAlias("upload_session")
@Document(UPLOAD_SESSION_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadSession extends AggregateRoot {

    private String ownerId;
    private String filename; // 带扩展名
    private String expectedHash; // 客户端整文件hash
    private long totalSize;
    private int chunkSize;
    private int totalChunks;
    private UploadStatus status;

    // 该字段不能加final
    // MongoDB 文档反序列化需要替换集合字段，final 集合会导致持久化后的 uploadedChunks 丢失
    private Set<Integer> uploadedChunks; // 存储chunkIndex，chunkIndex是0-based

    private UploadSession(
            String ownerId,
            String filename,
            String expectedHash,
            long totalSize,
            int chunkSize,
            int totalChunks,
            UserContext userContext
    ) {
        super(newUploadSessionId(), userContext);
        this.ownerId = ownerId;
        this.filename = filename;
        this.expectedHash = expectedHash;
        this.totalSize = totalSize;
        this.chunkSize = chunkSize;
        this.totalChunks = totalChunks;
        this.status = UploadStatus.INIT;
        this.uploadedChunks = new HashSet<>();
    }

    public static UploadSession create(
            String ownerId,
            String filename,
            String expectedHash,
            long totalSize,
            int chunkSize,
            int totalChunks,
            UserContext userContext
    ) {
        return new UploadSession(
                ownerId, filename, expectedHash,
                totalSize, chunkSize, totalChunks, userContext
        );
    }

    public static UploadSession createSingle(String filename, long totalSize, UserContext userContext) {
        return new UploadSession(
                userContext.getUid(), filename, "",
                totalSize, (int) totalSize, 1, userContext
        );
    }

    public static String newUploadSessionId() {
        return UPLOAD_SESSION_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public boolean isCompleted() {
        return this.status == UploadStatus.COMPLETED;
    }

    public void complete(UserContext userContext) {
        if (isCompleted()) {
            throw new MyException(UPLOAD_ALREADY_COMPLETED, "Upload already completed", "uploadId", getId());
        }

        this.status = UploadStatus.COMPLETED;
        addOpsLog("上传完成", userContext);
    }

    public boolean containsUploadedChunk(int chunkIndex) {
        return uploadedChunks.contains(chunkIndex);
    }

    /**
     * 将分片保存至磁盘，多次调用会重复保存<br>
     * 分片命名约定：数字顺序命名（0,1,2...）
     *
     * @param chunkIndex 分片Index
     * @param chunk      分片
     * @param chunkDir   分片存储路径
     */
    public void saveChunk(int chunkIndex, MultipartFile chunk, String chunkDir) {
        Path path = Paths.get(chunkDir, getId());
        try {
            Files.createDirectories(path);
            Path chunkPath = path.resolve(String.valueOf(chunkIndex));
            chunk.transferTo(chunkPath.toFile());
        } catch (IOException ex) {
            throw new MyException(SAVE_CHUNK_FAILED, "Save chunk failed", "uploadId", getId(), "chunkIndex", chunkIndex);
        }
        markChunkUploaded(chunkIndex);
    }

    private void markChunkUploaded(int chunkIndex) {
        uploadedChunks.add(chunkIndex);
        if (this.status != UploadStatus.UPLOADING) {
            this.status = UploadStatus.UPLOADING;
        }
    }

    public void checkAllChunksUploaded() {
        if (isEmpty(uploadedChunks)) {
            throw new MyException(MERGE_CHUNKS_FAILED, "No chunks uploaded",
                    "uploadId", getId());
        }

        if (uploadedChunks.size() != totalChunks) {
            throw new MyException(MERGE_CHUNKS_FAILED, "Chunks not complete",
                    "uploadId", getId(), "uploaded", uploadedChunks.size(), "total", totalChunks);
        }

        // 校验是否包含 0 ~ totalChunks-1
        for (int i = 0; i < totalChunks; i++) {
            if (!uploadedChunks.contains(i)) {
                throw new MyException(MERGE_CHUNKS_FAILED, "Missing chunk",
                        "uploadId", getId(), "missingChunk", i);
            }
        }
    }

    public void checkHash(String actualHash) {
        if (!this.expectedHash.equals(actualHash)) {
            throw new MyException(
                    FILE_HASH_MISMATCH,
                    "File hash mismatch",
                    "expected", expectedHash,
                    "actual", actualHash
            );
        }
    }

}
