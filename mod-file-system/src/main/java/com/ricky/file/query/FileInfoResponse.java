package com.ricky.file.query;

import com.ricky.common.domain.marker.Response;
import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.FileStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileInfoResponse implements Response {

    String filename;
    Long size;
    FileStatus status;
    FileCategory category;
    LocalDateTime createTime;
    LocalDateTime updateTime;

}
