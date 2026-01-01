package com.ricky.file.domain;

import lombok.Getter;

import static com.ricky.file.domain.FileExtension.*;

/**
 * @brief MIME类型枚举
 */
@Getter
public enum MimeType {

    // 图片类型
    IMAGE_JPEG("image/jpeg", JPG, JPEG),
    IMAGE_PNG("image/png", PNG),
    IMAGE_GIF("image/gif", GIF),
    IMAGE_BMP("image/bmp", BMP),
    IMAGE_WEBP("image/webp", WEBP),
    IMAGE_SVG("image/svg+xml", SVG),

    // 文档类型
    APPLICATION_PDF("application/pdf", PDF),
    APPLICATION_DOC("application/msword", DOC),
    APPLICATION_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOCX),
    APPLICATION_XLS("application/vnd.ms-excel", XLS),
    APPLICATION_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", XLSX),
    APPLICATION_PPT("application/vnd.ms-powerpoint", PPT),
    APPLICATION_PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", PPTX),

    // 文本类型
    TEXT_PLAIN("text/plain", TXT),
    TEXT_CSV("text/csv", CSV),
    TEXT_HTML("text/html", HTML, HTM),
    APPLICATION_XML("application/xml", XML),
    APPLICATION_JSON("application/json", JSON),

    // 压缩类型
    APPLICATION_ZIP("application/zip", ZIP),
    APPLICATION_RAR("application/x-rar-compressed", RAR),
    APPLICATION_7Z("application/x-7z-compressed", SEVEN_Z),
    APPLICATION_TAR("application/x-tar", TAR),
    APPLICATION_GZIP("application/gzip", GZ),

    // 音视频类型
    VIDEO_MP4("video/mp4", MP4),
    AUDIO_MPEG("audio/mpeg", MP3),
    VIDEO_AVI("video/x-msvideo", AVI),
    AUDIO_WAV("audio/wav", WAV),

    // 其他
    APPLICATION_EXE("application/x-msdownload", EXE),
    APPLICATION_BIN("application/octet-stream", BIN),
    APPLICATION_OCTET_STREAM("application/octet-stream");

    private final String contentType;
    private final FileExtension[] fileExtensions;

    MimeType(String contentType, FileExtension... fileExtensions) {
        this.contentType = contentType;
        this.fileExtensions = fileExtensions;
    }

    /**
     * 从MIME类型字符串获取枚举
     */
    public static MimeType fromString(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return APPLICATION_OCTET_STREAM;
        }

        String mime = mimeType.toLowerCase().trim();
        for (MimeType type : values()) {
            if (type.contentType.equals(mime)) {
                return type;
            }
        }
        return APPLICATION_OCTET_STREAM;
    }

    /**
     * 从扩展名获取MIME类型
     */
    public static MimeType fromExtension(FileExtension extension) {
        if (extension == null || extension == UNKNOWN) {
            return APPLICATION_OCTET_STREAM;
        }

        for (MimeType mimeType : values()) {
            for (FileExtension supportedExt : mimeType.fileExtensions) {
                if (supportedExt == extension) {
                    return mimeType;
                }
            }
        }
        return APPLICATION_OCTET_STREAM;
    }
}