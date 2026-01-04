package com.ricky.folder.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FOLDER_RENAMED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FOLDER_RENAMED;

@Getter
@TypeAlias(FOLDER_RENAMED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderRenamedEvent extends DomainEvent {

    private String folderId;

    public FolderRenamedEvent(String folderId, UserContext userContext) {
        super(FOLDER_RENAMED, userContext);
        this.folderId = folderId;
    }
}
