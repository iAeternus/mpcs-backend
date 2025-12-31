package com.ricky.folder.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.event.DomainEventType.FOLDER_DELETED;

@Getter
@TypeAlias("FOLDER_DELETED_EVENT")
@JsonTypeDefine("FOLDER_DELETED_EVENT")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderDeletedEvent extends DomainEvent {

    private String folderId;

    public FolderDeletedEvent(String folderId, UserContext userContext) {
        super(FOLDER_DELETED, userContext);
        this.folderId = folderId;
    }

}
