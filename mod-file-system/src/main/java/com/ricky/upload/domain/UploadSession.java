package com.ricky.upload.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String ossUploadId; // OSS multipart upload ID
    private UploadStatus status;
    private Set<Integer> uploadedChunks; // 存储chunkIndex，chunkIndex是0-based

    public UploadSession(
            String ownerId,
            String filename,
            String expectedHash,
            long totalSize,
            int chunkSize,
            int totalChunks,
            String ossUploadId,
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
        this.ossUploadId = ossUploadId;
    }

    public static String newUploadSessionId() {
        return UPLOAD_SESSION_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public boolean isCompleted() {
        return this.status == UploadStatus.COMPLETED;
    }

    public void checkOwner(String userId) {
        if (!this.ownerId.equals(userId)) {
            throw MyException.accessDeniedException();
        }
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
