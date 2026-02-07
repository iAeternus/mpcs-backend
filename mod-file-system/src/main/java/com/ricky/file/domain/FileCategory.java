package com.ricky.file.domain;

import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;

import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.file.domain.FileExtension.*;
import static java.util.Collections.unmodifiableMap;

@Getter
public enum FileCategory {

    IMAGE("图片文件", JPG, JPEG, PNG, GIF, BMP, WEBP, SVG),
    DOCUMENT("文档文件", PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX),
    TEXT("文本文件", TXT, CSV, HTML, HTM, XML, JSON),
    ARCHIVE("压缩文件", ZIP, RAR, SEVEN_Z, TAR, GZ),
    MEDIA("媒体文件", MP4, MP3, AVI, WAV),
    EXECUTABLE("可执行文件", EXE, BIN),
    UNKNOWN("未知文件类型");

    private final String name;
    private final FileExtension[] extensions;

    private static final Map<FileExtension, FileCategory> EXTENSION_TO_CATEGORY;

    static {
        Map<FileExtension, FileCategory> map = new EnumMap<>(FileExtension.class);
        for (FileCategory category : values()) {
            for (FileExtension extension : category.extensions) {
                map.put(extension, category);
            }
        }
        EXTENSION_TO_CATEGORY = unmodifiableMap(map);
    }

    FileCategory(String name, FileExtension... extensions) {
        this.name = name;
        this.extensions = extensions;
    }

    public static FileCategory fromExtension(FileExtension extension) {
        if (isNull(extension)) {
            return UNKNOWN;
        }
        return EXTENSION_TO_CATEGORY.getOrDefault(extension, UNKNOWN);
    }

    public static FileCategory fromExtensionString(String extension) {
        if (isBlank(extension)) {
            return UNKNOWN;
        }
        FileExtension ext = FileExtension.fromExtension(extension);
        return fromExtension(ext);
    }

    public static FileCategory fromFilename(String filename) {
        if (isBlank(filename)) {
            return UNKNOWN;
        }
        FileExtension ext = FileExtension.fromFilename(filename);
        return fromExtension(ext);
    }

    public boolean isPreviewable() {
        return switch (this) {
            case IMAGE, TEXT, DOCUMENT -> true;
            default -> false;
        };
    }

}