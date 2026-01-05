package com.ricky.upload.domain;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.file.domain.StorageId;
import com.ricky.file.domain.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @brief 文件数据存储
 */
public interface FileStorage {

    StorageId store(MultipartFile multipartFile);

    StoredFile mergeChunksAndStore(UploadSession session, Path chunkDir);

    GridFSFile findFile(StorageId storageId);

    InputStream getFileStream(StorageId storageId);

    void delete(StorageId storageId);

    void delete(List<StorageId> storageIds);

    boolean exists(StorageId storageId);
}
