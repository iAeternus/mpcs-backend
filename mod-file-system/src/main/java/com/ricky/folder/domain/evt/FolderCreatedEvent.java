package com.ricky.folder.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FOLDER_CREATED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FOLDER_CREATED;

@Getter
@TypeAlias(FOLDER_CREATED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderCreatedEvent extends DomainEvent {

    private String folderId;

    public FolderCreatedEvent(String folderId, UserContext userContext) {
        super(FOLDER_CREATED, userContext);
        this.folderId = folderId;
    }


}
