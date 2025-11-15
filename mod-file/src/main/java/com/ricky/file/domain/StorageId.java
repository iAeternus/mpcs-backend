package com.ricky.file.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * @brief 文件存储ID
 * @note 修改文件内容存储方式时，修改与这里相关的代码
 */
@Value
public class StorageId {

    @NotBlank
    String value;

}
