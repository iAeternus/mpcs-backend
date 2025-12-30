package com.ricky.file.domain.metadata;

import com.ricky.file.domain.MimeType;

public class GeneralMetadata extends Metadata {

    public GeneralMetadata(long size,
                           MimeType mimeType,
                           String hash,
                           boolean multipart,
                           int partCount) {
        super(size, mimeType, hash, multipart, partCount);
    }

    @Override
    public String summary() {
        return "General";
    }
}