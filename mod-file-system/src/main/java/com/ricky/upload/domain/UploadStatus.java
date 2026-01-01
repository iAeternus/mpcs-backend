package com.ricky.upload.domain;

/**
 * 分片上传状态
 */
public enum UploadStatus {

    /**
     * 初始化分片上传
     */
    INIT,

    /**
     * 正在分片上传
     */
    UPLOADING,

    /**
     * 分片上传完成
     */
    COMPLETED
}
