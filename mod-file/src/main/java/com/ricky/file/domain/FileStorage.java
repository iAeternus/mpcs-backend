package com.ricky.file.domain;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @brief 文件数据存储
 */
public interface FileStorage {
    StorageId store(MultipartFile multipartFile);

    GridFSFile findFile(StorageId storageId);

    InputStream getFileStream(StorageId storageId);

    void delete(StorageId storageId);
}
