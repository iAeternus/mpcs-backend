package com.ricky.file.domain.storage;

/**
 * @brief 文件存储ID
 * @note 修改文件内容存储方式时，修改与这里相关的代码
 */
public sealed interface StorageId
        permits GridFsStorageId, OssStorageId {

    String getValue();

}
