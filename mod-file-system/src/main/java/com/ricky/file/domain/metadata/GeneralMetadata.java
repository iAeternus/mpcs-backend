package com.ricky.file.domain.metadata;

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