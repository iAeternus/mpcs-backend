package com.ricky.file.domain.metadata;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @brief 图片元数据
 */
@Getter
public class ImageMetadata extends Metadata {

    private final int width;
    private final int height;

    public ImageMetadata(Long size, String mimeType, String hash, Long checksum, int width, int height) {
        super(size, mimeType, hash, LocalDateTime.now(), LocalDateTime.now(), checksum, false, 1);
        this.width = width;
        this.height = height;
    }

    @Override
    public String summary() {
        return String.format("Image[%dx%d]", width, height);
    }
}
