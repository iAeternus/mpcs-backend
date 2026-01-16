package com.ricky.file.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @brief 文件聚合根资源库
 */
public interface FileRepository {

    void save(File file);

    boolean existsByHash(String hash);

    File cachedById(String fileId);

    File byId(String fileId);

    Optional<StorageId> byFileHashOptional(String hash);

    List<File> listByFileHash(String hash);

    void delete(File file);

    void delete(List<File> files);

    List<File> byIds(Set<String> fileIds);

    List<File> listByStorageId(StorageId storageId);

    Map<StorageId, List<File>> listByStorageIds(List<StorageId> storageIds);

    boolean exists(String fileId);
}
