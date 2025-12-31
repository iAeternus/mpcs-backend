package com.ricky.file.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.event.DomainEventType.FILE_DELETED;
import static com.ricky.common.event.DomainEventType.FOLDER_DELETED;

@Getter
@TypeAlias("FILE_DELETED_EVENT")
@JsonTypeDefine("FILE_DELETED_EVENT")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDeletedEvent extends DomainEvent {

    private String fileId;

    public FileDeletedEvent(String fileId, UserContext userContext) {
        super(FILE_DELETED, userContext);
        this.fileId = fileId;
    }

}
