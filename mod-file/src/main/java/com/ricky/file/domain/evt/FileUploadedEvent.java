package com.ricky.file.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
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
    String filename;
    String hash;
    Long size;

    public FileUploadedEvent(String fileId, String filename, String hash, Long size, UserContext userContext) {
        super(FILE_UPLOADED, userContext);
        this.fileId = fileId;
        this.filename = filename;
        this.hash = hash;
        this.size = size;
    }

}
