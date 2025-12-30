package com.ricky.file.domain.metadata;

import com.ricky.file.domain.MimeType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @brief 图片元数据
 */
@Getter
public class ImageMetadata extends Metadata {

    private final int width;
    private final int height;

    public ImageMetadata(long size,
                         MimeType mimeType,
                         String hash,
                         boolean multipart,
                         int partCount,
                         int width,
                         int height) {
        super(size, mimeType, hash, multipart, partCount);
        this.width = width;
        this.height = height;
    }

    @Override
    public String summary() {
        return String.format("Image[%dx%d]", width, height);
    }
}
