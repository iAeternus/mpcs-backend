package com.ricky.upload.handler.tasks.extracttext;

import java.io.IOException;
import java.io.InputStream;

public interface TextExtractor {

    /**
     * 读取文件流，取到其中的文字信息
     *
     * @param inputStream  文件流
     * @param textFilePath 存储文本文件
     */
    void extract(InputStream inputStream, String textFilePath) throws IOException;

}
