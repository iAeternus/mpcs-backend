package com.ricky.common.oss;

import java.io.InputStream;

public interface OssService {

    /**
     * 上传对象
     */
    void putObject(String bucket, String objectKey, InputStream input, long size, String contentType);

    /**
     * 获取对象流
     */
    InputStream getObject(String bucket, String objectKey);

    /**
     * 删除对象
     */
    void deleteObject(String bucket, String objectKey);

    /**
     * 是否存在
     */
    boolean exists(String bucket, String objectKey);

}
