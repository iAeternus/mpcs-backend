package com.ricky.file.domain.metadata;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 文件元数据
 */
@Deprecated
@Getter
@NoArgsConstructor(access = PRIVATE, force = true)
public abstract class Metadata {

    protected final long size;
    protected final MimeType mimeType;
    protected final String hash; // 内容hash
    protected final LocalDateTime uploadedAt; // 上传时间
    protected final boolean multipart; // 是否分片上传
    protected final int partCount; // 分片个数，若isMultipart=false则为1

    protected Metadata(long size,
                       MimeType mimeType,
                       String hash,
                       boolean multipart,
                       int partCount) {
        this.size = size;
        this.mimeType = mimeType;
        this.hash = hash;
        this.multipart = multipart;
        this.partCount = partCount;
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * 人类可读摘要
     */
    public abstract String summary();

}
