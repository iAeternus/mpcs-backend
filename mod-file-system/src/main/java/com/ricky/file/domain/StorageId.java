package com.ricky.file.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.bson.types.ObjectId;

/**
 * @brief 文件存储ID
 * @note 修改文件内容存储方式时，修改与这里相关的代码
 */
@Value
public class StorageId {

    @NotNull
    String value;

    public ObjectId toObjectId(){
        return new ObjectId(value);
    }

}
