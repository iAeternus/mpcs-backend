package com.ricky.upload.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
import com.ricky.file.domain.FileCategory;
import com.ricky.file.domain.StorageId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.event.DomainEventType.FILE_UPLOADED;

/**
 * @brief 文件上传事件
 */
@Getter
@TypeAlias("FILE_UPLOAD_EVENT")
@JsonTypeDefine("FILE_UPLOAD_EVENT")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadedEvent extends DomainEvent {

    String fileId;
    StorageId storageId;
    FileCategory category;

    public FileUploadedEvent(String fileId, StorageId storageId, FileCategory category, UserContext userContext) {
        super(FILE_UPLOADED, userContext);
        this.fileId = fileId;
        this.storageId = storageId;
        this.category = category;
    }

}
