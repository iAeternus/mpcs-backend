package com.ricky.folder.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.FOLDER_HIERARCHY_CHANGED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.FOLDER_HIERARCHY_CHANGED;

@Getter
@TypeAlias(FOLDER_HIERARCHY_CHANGED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchyChangedEvent extends DomainEvent {

    private String customId;
    private Set<String> changedFolderIds;
    private Set<String> changedFileIds;

    public FolderHierarchyChangedEvent(String customId,
                                       Set<String> changedFolderIds,
                                       Set<String> changedFileIds,
                                       UserContext userContext) {
        super(FOLDER_HIERARCHY_CHANGED, userContext);
        this.customId = customId;
        this.changedFolderIds = changedFolderIds;
        this.changedFileIds = changedFileIds;
    }
}
