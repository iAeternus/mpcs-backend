package com.ricky.folder.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.json.JsonTypeDefine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.event.DomainEventType.FOLDER_CREATED;

@Getter
@TypeAlias("FOLDER_CREATED_EVENT")
@JsonTypeDefine("FOLDER_CREATED_EVENT")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderCreatedEvent extends DomainEvent {

    private String folderId;

    public FolderCreatedEvent(String folderId, UserContext userContext) {
        super(FOLDER_CREATED, userContext);
        this.folderId = folderId;
    }


}
