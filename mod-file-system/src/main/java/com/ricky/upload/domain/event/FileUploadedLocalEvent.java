package com.ricky.upload.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.LocalDomainEvent;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.storage.StorageId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @brief 文件上传事件
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadedLocalEvent extends LocalDomainEvent {

    String fileId;
    StorageId storageId;
    FileCategory category;

    public FileUploadedLocalEvent(File file, String fileId, StorageId storageId, FileCategory category, UserContext userContext) {
        super(file, userContext);
        this.fileId = fileId;
        this.storageId = storageId;
        this.category = category;
    }

}
