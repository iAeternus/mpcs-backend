package com.ricky.folderhierarchy.domain.evt;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.FOLDER_HIERARCHY_CHANGED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FOLDER_HIERARCHY_CHANGED;

@Getter
@TypeAlias(FOLDER_HIERARCHY_CHANGED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchyChangedEvent extends DomainEvent {

    private String userId;

    public FolderHierarchyChangedEvent(String userId, UserContext userContext) {
        super(FOLDER_HIERARCHY_CHANGED, userContext);
        this.userId = userId;
    }
}
