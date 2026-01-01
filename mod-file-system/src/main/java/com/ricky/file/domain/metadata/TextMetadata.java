package com.ricky.file.domain.metadata;

import com.ricky.file.domain.MimeType;
import lombok.Getter;

/**
 * @brief 文本文件元数据
 */
@Getter
public class TextMetadata extends Metadata {

    public TextMetadata(long size,
                        MimeType mimeType,
                        String hash,
                        boolean multipart,
                        int partCount) {
        super(size, mimeType, hash, multipart, partCount);
    }

    @Override
    public String summary() {
        return "Text";
    }
}
