package com.ricky.file.infra;

import com.ricky.file.domain.StorageId;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief 文件数据存储
 */
public interface FileStorage {
    StorageId store(MultipartFile multipartFile);
}
