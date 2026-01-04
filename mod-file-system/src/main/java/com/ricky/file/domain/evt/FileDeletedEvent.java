package com.ricky.file.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FILE_DELETED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FILE_DELETED;

@Getter
@TypeAlias(FILE_DELETED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileDeletedEvent extends DomainEvent {

    private String fileId;

    public FileDeletedEvent(String fileId, UserContext userContext) {
        super(FILE_DELETED, userContext);
        this.fileId = fileId;
    }

}
