package com.ricky.file.domain;

import lombok.Getter;

import static java.util.Arrays.stream;

/**
 * @brief 文件类型
 */
@Getter
public enum FileType {

    IMAGE("image/jpeg", "image/png", "image/gif"),
    PDF("application/pdf"),
    TEXT("text/plain"),
    UNKNOWN("application/octet-stream");

    private final String[] mimeTypes;

    FileType(String... mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public static FileType fromMimeType(String mimeType) {
        return stream(values())
                .filter(t -> stream(t.mimeTypes)
                        .anyMatch(m -> m.equalsIgnoreCase(mimeType)))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
