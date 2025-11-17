package com.ricky.file.domain.metadata;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 文件元数据
 */
@Getter
@NoArgsConstructor(access = PRIVATE, force = true)
public abstract class Metadata {

    protected final long size;
    protected final String mimeType;
    protected final String hash; // 内容hash
    protected final LocalDateTime uploadedAt; // 上传时间
    protected final LocalDateTime lastAccessedAt; // 上次访问时间
    protected final long checksum; // 校验和
    protected final boolean isMultipart; // 是否分片上传
    protected final int partCount; // 分片个数，若isMultipart=false则为1

    protected Metadata(long size,
                       String mimeType,
                       String hash,
                       LocalDateTime uploadedAt,
                       LocalDateTime lastAccessedAt,
                       long checksum,
                       boolean isMultipart,
                       int partCount) {
        this.size = size;
        this.mimeType = mimeType;
        this.hash = hash;
        this.uploadedAt = uploadedAt;
        this.lastAccessedAt = lastAccessedAt;
        this.checksum = checksum;
        this.isMultipart = isMultipart;
        this.partCount = partCount;
    }

    /**
     * 元数据描述方法
     */
    public abstract String summary();

}
