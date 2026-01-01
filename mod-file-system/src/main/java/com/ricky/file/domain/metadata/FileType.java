package com.ricky.file.domain.metadata;

import com.ricky.file.domain.MimeType;
import lombok.Getter;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.file.domain.MimeType.*;
import static java.util.Arrays.stream;

/**
 * @brief 文件类型
 */
@Getter
public enum FileType {

    IMAGE("图片文件",
            IMAGE_JPEG, IMAGE_PNG, IMAGE_GIF,
            IMAGE_BMP, IMAGE_WEBP, IMAGE_SVG),

    DOCUMENT("文档文件",
            APPLICATION_PDF, APPLICATION_DOC, APPLICATION_DOCX,
            APPLICATION_XLS, APPLICATION_XLSX, APPLICATION_PPT, APPLICATION_PPTX),

    TEXT("文本文件",
            TEXT_PLAIN, TEXT_CSV, TEXT_HTML,
            APPLICATION_XML, APPLICATION_JSON),

    ARCHIVE("压缩文件",
            APPLICATION_ZIP, APPLICATION_RAR, APPLICATION_7Z,
            APPLICATION_TAR, APPLICATION_GZIP),

    MEDIA("媒体文件",
            VIDEO_MP4, AUDIO_MPEG, VIDEO_AVI, AUDIO_WAV),

    UNKNOWN("其他文件");

    private final String name;
    private final Set<MimeType> mimeTypes;

    FileType(String name, MimeType... mimeTypes) {
        this.name = name;
        this.mimeTypes = stream(mimeTypes).collect(toImmutableSet());
    }

    public static FileType fromMimeType(MimeType mimeType) {
        for (FileType fileType : values()) {
            if (fileType.supportsMimeType(mimeType)) {
                return fileType;
            }
        }
        return UNKNOWN;
    }

    public static FileType fromContentType(String contentType) {
        return fromMimeType(MimeType.fromString(contentType));
    }

    public boolean supportsMimeType(MimeType mimeType) {
        return this == UNKNOWN || mimeTypes.contains(mimeType);
    }

}
