package com.ricky.file.infra;

import com.ricky.file.domain.File;

/**
 * @brief 文件聚合根资源库
 */
public interface FileRepository {

    void save(File file);

    boolean existsByHash(String hash);

    File cachedById(String fileId);

    File byId(String fileId);
}
