package com.ricky.file.domain.metadata;

import java.time.LocalDateTime;

public class GeneralMetadata extends Metadata {

    public GeneralMetadata(Long size, String mimeType, String hash, Long checksum) {
        super(size, mimeType, hash, LocalDateTime.now(), LocalDateTime.now(), checksum, false, 1);
    }

    @Override
    public String summary() {
        return String.format("File[%s]", mimeType);
    }
}