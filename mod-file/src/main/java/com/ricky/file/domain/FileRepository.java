package com.ricky.file.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @brief 文件聚合根资源库
 */
public interface FileRepository {

    void save(File file);

    boolean existsByHash(String hash);

    File cachedById(String fileId);

    File byId(String fileId);

    List<File> listByFileHash(String hash);

    void delete(List<File> files);
}
