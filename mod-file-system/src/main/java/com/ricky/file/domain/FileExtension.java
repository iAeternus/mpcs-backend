package com.ricky.file.domain;

import lombok.Getter;

/**
 * @brief 文件扩展名枚举
 */
@Getter
public enum FileExtension {

    // 图片格式
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif"),
    BMP("bmp"),
    WEBP("webp"),
    SVG("svg"),

    // 文档格式
    PDF("pdf"),
    DOC("doc"),
    DOCX("docx"),
    XLS("xls"),
    XLSX("xlsx"),
    PPT("ppt"),
    PPTX("pptx"),

    // 文本格式
    TXT("txt"),
    CSV("csv"),
    HTML("html"),
    HTM("htm"),
    XML("xml"),
    JSON("json"),

    // 压缩格式
    ZIP("zip"),
    RAR("rar"),
    SEVEN_Z("7z"),
    TAR("tar"),
    GZ("gz"),

    // 音视频格式
    MP4("mp4"),
    MP3("mp3"),
    AVI("avi"),
    WAV("wav"),

    // 其他
    EXE("exe"),
    BIN("bin"),
    UNKNOWN("unknown");

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    /**
     * 从文件扩展名字符串获取扩展名枚举
     */
    public static FileExtension fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return UNKNOWN;
        }

        String ext = extension.toLowerCase().trim();
        for (FileExtension fileExtension : values()) {
            if (fileExtension.value.equals(ext)) {
                return fileExtension;
            }
        }
        return UNKNOWN;
    }

    /**
     * 从文件名获取文件扩展名枚举
     */
    public static FileExtension fromFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return UNKNOWN;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            String extension = filename.substring(lastDotIndex + 1).toLowerCase();
            return fromExtension(extension);
        }
        return UNKNOWN;
    }

}