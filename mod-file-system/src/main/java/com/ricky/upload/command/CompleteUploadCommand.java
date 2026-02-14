package com.ricky.upload.command;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.filename.Filename;
import com.ricky.common.validation.id.Id;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteUploadCommand implements Command {

    @NotBlank
    @Id(FOLDER_ID_PREFIX)
    String parentId;

    @NotBlank
    String fileHash; // 整文件 hash

    @Positive
    long totalSize;

    /**
     * 分块上传场景
     */
    @Nullable
    String uploadId;

    /**
     * 快速上传场景：客户端原始文件名
     */
    @Nullable
    @Filename
    String fileName;

    /**
     * 快速上传场景：可选的存储 ID 值以实现兼容性
     */
    @Nullable
    String storageId;

    public boolean isFastUpload() {
        return uploadId == null || uploadId.isBlank();
    }

    public boolean isChunkUpload() {
        return uploadId != null && !uploadId.isBlank();
    }

}
