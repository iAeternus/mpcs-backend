package com.ricky.upload.domain.tasks.extracttext.impl;

import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.storage.StorageId;
import com.ricky.upload.domain.tasks.extracttext.TextExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
public abstract class AbstractTextExtractor implements TextExtractor {

    @Override
    public boolean supports(FileCategory category) {
        return getSupportedCategory() == category;
    }

    @Override
    public String extract(StorageId storageId, InputStream inputStream, String textFileDir) throws IOException {
        ensureDirectoryExists(textFileDir);

        String filePath = generateFilePath(storageId, textFileDir);
        Path path = Paths.get(filePath);

        // 若文件已存在，则直接返回路径
        if (Files.exists(path)) {
            log.debug("使用缓存文本文件: storageId={}, path={}", storageId.getValue(), filePath);
            return filePath;
        }

        // 若文件不存在，则执行提取
        doExtract(inputStream, filePath);
        log.debug("文本提取完成并缓存: storageId={}, path={}", storageId.getValue(), filePath);
        return filePath;
    }

    /**
     * 生成固定格式的文件路径
     * 格式: {textFileDir}/{storageId}.txt
     */
    private String generateFilePath(StorageId storageId, String textFileDir) {
        String filename = String.format("%s.txt", storageId.getValue());
        return Paths.get(textFileDir, filename).toString();
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoryExists(String textFileDir) throws IOException {
        Path dir = Paths.get(textFileDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.debug("创建文本提取目录: {}", textFileDir);
        }
    }

    /**
     * 获取支持的文件类型
     */
    public abstract FileCategory getSupportedCategory();

    /**
     * 执行具体的文本提取逻辑
     */
    protected abstract void doExtract(InputStream inputStream, String filePath) throws IOException;

    @Override
    public void clearCache(StorageId storageId, String textFileDir) throws IOException {
        String filePath = generateFilePath(storageId, textFileDir);
        Path path = Paths.get(filePath);

        if (Files.exists(path)) {
            Files.delete(path);
            log.info("删除缓存文件成功: storageId={}, path={}", storageId.getValue(), filePath);
        } else {
            log.debug("缓存文件不存在，无需删除: storageId={}, path={}", storageId.getValue(), filePath);
        }
    }

    @Override
    public Optional<Long> getCacheLastModified(StorageId storageId, String textFileDir) {
        String filePath = generateFilePath(storageId, textFileDir);
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Files.getLastModifiedTime(path).toMillis());
        } catch (IOException e) {
            log.warn("获取缓存文件最后修改时间失败: path={}", filePath, e);
            return Optional.empty();
        }
    }
}