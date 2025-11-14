package com.ricky.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 文件元数据
 */
@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MetaData {

    Long size;
    String mimeType;
    String hash; // 内容hash
    LocalDateTime uploadedAt; // 上传时间
    LocalDateTime lastAccessedAt; // 上次访问时间
    Long checksum; // 校验和
    Boolean isMultipart; // 是否分片上传
    Integer partCount; // 分片个数，若isMultipart=false则为1

    // TODO

}
