package com.ricky.group.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.GROUP_DELETED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.GROUP_DELETED;

@Getter
@TypeAlias(GROUP_DELETED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupDeletedEvent extends DomainEvent {

    private String groupId;

    public GroupDeletedEvent(String groupId, UserContext userContext) {
        super(GROUP_DELETED, userContext);
        this.groupId = groupId;
    }

}
