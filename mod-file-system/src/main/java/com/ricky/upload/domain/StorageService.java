package com.ricky.upload.domain;

import com.ricky.file.domain.storage.StorageId;
import com.ricky.file.domain.storage.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @brief 文件数据存储服务
 */
public interface StorageService {

    StorageId store(MultipartFile multipartFile);

    StoredFile mergeChunksAndStore(UploadSession session, Path chunkDir);

    String initMultipartUpload(String filename);

    String uploadPart(String uploadId, int partNumber, MultipartFile chunk);

    StoredFile completeMultipartUpload(String uploadId, String filename, long totalSize);

    void abortMultipartUpload(String uploadId);

    InputStream getFileStream(StorageId storageId);

    void delete(StorageId storageId);

    void delete(List<StorageId> storageIds);

    boolean exists(StorageId storageId);
}
