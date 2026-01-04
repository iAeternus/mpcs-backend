package com.ricky.upload.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.filename.Filename;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InitUploadCommand implements Command {

    @NotBlank
    @Filename
    String fileName;

    @NotBlank
    String fileHash; // 整文件 hash

    @Min(0)
    @NotNull
    Long totalSize;

    @Min(0)
    @NotNull
    Integer chunkSize;

    @Min(0)
    @NotNull
    Integer totalChunks;

}
