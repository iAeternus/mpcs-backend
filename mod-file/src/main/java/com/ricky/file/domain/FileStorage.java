package com.ricky.file.domain;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.ricky.upload.domain.UploadSession;
import org.bson.types.ObjectId;
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
}
