package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.metadata.FileType;
import com.ricky.file.domain.metadata.Metadata;
import org.springframework.web.multipart.MultipartFile;

/**
 * 元数据提取策略接口
 */
public interface MetadataExtractor {

    /**
     * 提取元数据
     *
     * @param context 元数据提取上下文
     * @return 元数据
     */
    Metadata extract(MetadataContext context);

    /**
     * 判断文件的contentType是否支持
     *
     * @param fileType 文件类型
     * @return true=支持 false=不支持
     */
    boolean supports(FileType fileType);

}
