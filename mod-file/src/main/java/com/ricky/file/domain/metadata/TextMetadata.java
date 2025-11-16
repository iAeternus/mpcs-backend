package com.ricky.file.domain.metadata;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @brief 文本文件元数据
 */
@Getter
public class TextMetadata extends Metadata {

    private final int wordCount;
    private final int characterCount;

    public TextMetadata(long size, String mimeType, String hash, long checksum, int wordCount, int characterCount) {
        super(size, mimeType, hash, LocalDateTime.now(), LocalDateTime.now(), checksum, false, 1);
        this.wordCount = wordCount;
        this.characterCount = characterCount;
    }

    @Override
    public String summary() {
        return String.format("Text[%d word, %d character]", wordCount, characterCount);
    }
}
