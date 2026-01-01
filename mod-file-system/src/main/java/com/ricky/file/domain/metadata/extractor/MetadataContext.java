package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.MimeType;
import lombok.Value;

import java.io.InputStream;

@Value
public class MetadataContext {

    String filename;
    MimeType mimeType;
    long size;
    String hash;
    Integer checksum;
    boolean multipart;
    int partCount;

    /**
     * 只有在需要读取文件内容时才提供
     */
    InputStream inputStream;

    public boolean hasStream() {
        return inputStream != null;
    }
}
