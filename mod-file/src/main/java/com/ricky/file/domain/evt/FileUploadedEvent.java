package com.ricky.file.domain.evt;

import com.ricky.common.domain.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.domain.event.DomainEventTypeEnum.FILE_UPLOADED_EVENT;

/**
 * @brief 文件上传事件
 */
@Getter
@TypeAlias("DOC_CREATED_EVENT")
@JsonTypeDefine("FILE_UPLOAD_EVENT")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadedEvent extends DomainEvent {

    String fileId;
    String filename;
    String hash;
    Long size;
    String mimeType;

    public FileUploadedEvent(String fileId, String filename, String hash, Long size, String mimeType) {
        super(FILE_UPLOADED_EVENT);
        this.fileId = fileId;
        this.filename = filename;
        this.hash = hash;
        this.size = size;
        this.mimeType = mimeType;
    }

}
