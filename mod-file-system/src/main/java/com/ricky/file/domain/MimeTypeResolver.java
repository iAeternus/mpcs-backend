package com.ricky.file.domain;

public interface MimeTypeResolver {

    /**
     * 将文件扩展名映射为 MIME type
     */
    String resolve(FileExtension extension);
}
