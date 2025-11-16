package com.ricky.file.domain.metadata.extractor;

import com.ricky.file.domain.metadata.Metadata;
import org.springframework.web.multipart.MultipartFile;

/**
 * @brief 元数据提取策略接口
 */
public interface MetadataExtractor {

    /**
     * @param file 文件
     * @return 元数据
     * @brief 提取元数据
     */
    Metadata extract(MultipartFile file);

    /**
     * @brief 判断文件的contentType是否支持
     * @param file 文件
     * @return true=支持 false=不支持
     */
    boolean supports(MultipartFile file);

}
