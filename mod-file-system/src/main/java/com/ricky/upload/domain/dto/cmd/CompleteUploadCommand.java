package com.ricky.upload.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.id.Id;
import com.ricky.common.validation.path.Path;
import com.ricky.file.domain.StorageId;
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
    @Id(pre = FOLDER_ID_PREFIX)
    String parentId;

    @NotBlank
    String filename;

    @NotBlank
    String fileHash; // 整文件 hash

    @Positive
    long totalSize;

    /**
     * 分片上传场景：必须提供
     */
    @Nullable
    String uploadId;

    /**
     * 秒传场景：必须提供
     */
    @Nullable
    StorageId storageId;

    public boolean isFastUpload() {
        return uploadId == null && storageId != null;
    }

    public boolean isChunkUpload() {
        return uploadId != null;
    }

}
