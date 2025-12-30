package com.ricky.file.domain.metadata;

import com.ricky.file.domain.MimeType;
import lombok.Getter;

/**
 * @brief PDF元数据
 */
@Getter
public class PdfMetadata extends Metadata {

    public PdfMetadata(long size,
                       MimeType mimeType,
                       String hash,
                       boolean multipart,
                       int partCount) {
        super(size, mimeType, hash, multipart, partCount);
    }

    @Override
    public String summary() {
        return "PDF";
    }
}
