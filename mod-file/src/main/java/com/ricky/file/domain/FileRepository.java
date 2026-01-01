package com.ricky.file.domain;

import java.util.List;
import java.util.Set;

/**
 * @brief 文件聚合根资源库
 */
public interface FileRepository {

    void save(File file);

    boolean existsByHash(String hash);

    File cachedById(String fileId);

    File byId(String fileId);

    HashCachedStorageIds cachedByFileHash(String hash);

    List<File> listByFileHash(String hash);

    void delete(List<File> files);

    List<File> byIds(Set<String> fileIds);
}
