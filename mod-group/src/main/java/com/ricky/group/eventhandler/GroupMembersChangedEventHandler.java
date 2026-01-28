package com.ricky.group.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupMembersChangedEventHandler extends AbstractDomainEventHandler<GroupMembersChangedEvent> {
    @Override
    protected void doHandle(GroupMembersChangedEvent event) {
        // TODO
    }
}
