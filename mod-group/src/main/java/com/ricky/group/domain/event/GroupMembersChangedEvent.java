package com.ricky.group.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.GROUP_MEMBERS_CHANGED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.GROUP_MEMBERS_CHANGED;

@Getter
@TypeAlias(GROUP_MEMBERS_CHANGED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupMembersChangedEvent extends DomainEvent {

    String groupId;
    List<String> userIds; // 变更的用户ID，可能包含重复或不存在的ID

    public GroupMembersChangedEvent(String groupId, List<String> userIds, UserContext userContext) {
        super(GROUP_MEMBERS_CHANGED, userContext);
        this.groupId = groupId;
        this.userIds = userIds;
    }
}
