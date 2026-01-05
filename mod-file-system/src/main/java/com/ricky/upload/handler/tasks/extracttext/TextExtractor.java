package com.ricky.upload.handler.tasks.extracttext;

import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.StorageId;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public interface TextExtractor {

    /**
     * 是否支持此文件类型
     */
    boolean supports(FileCategory category);

    /**
     * 提取文本，创建文本文件缓存
     *
     * @param storageId   存储ID
     * @param inputStream 文件输入流
     * @param textFileDir 文本文件缓存路径
     * @return 文本文件路径
     * @throws IOException 若提取失败或创建文件失败，则抛出异常
     */
    String extract(StorageId storageId, InputStream inputStream, String textFileDir) throws IOException;

    /**
     * 清除文本文件缓存
     *
     * @param storageId   存储ID
     * @param textFileDir 文本文件缓存路径
     * @throws IOException 若清除失败，则抛出异常
     */
    void clearCache(StorageId storageId, String textFileDir) throws IOException;

    /**
     * 获取缓存文件的最后修改时间
     *
     * @param storageId   存储ID
     * @param textFileDir 文本文件缓存路径
     * @return 若获取失败，返回 Optional.empty()
     */
    Optional<Long> getCacheLastModified(StorageId storageId, String textFileDir);

}
