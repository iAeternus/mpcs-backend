package com.ricky.file.domain.metadata;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @brief PDF元数据
 */
@Getter
public class PdfMetadata extends Metadata {

    private final int pageCount;

    public PdfMetadata(Long size, String mimeType, String hash, Long checksum, int pageCount) {
        super(size, mimeType, hash, LocalDateTime.now(), LocalDateTime.now(), checksum, false, 1);
        this.pageCount = pageCount;
    }

    @Override
    public String summary() {
        return String.format("Pdf[%d pages]", pageCount);
    }
}
