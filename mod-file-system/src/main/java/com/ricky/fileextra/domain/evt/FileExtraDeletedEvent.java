package com.ricky.fileextra.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FILE_EXTRA_DELETED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FILE_EXTRA_DELETED;

@Getter
@TypeAlias(FILE_EXTRA_DELETED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExtraDeletedEvent extends DomainEvent {

    private String textFilePath;

    public FileExtraDeletedEvent(String textFilePath, UserContext userContext) {
        super(FILE_EXTRA_DELETED, userContext);
        this.textFilePath = textFilePath;
    }

}
