package com.ricky.group.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.group.domain.event.GroupDeletedEvent;
import com.ricky.group.domain.task.DeleteTeamSpaceTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupDeletedEventHandler extends AbstractDomainEventHandler<GroupDeletedEvent> {

    private final DeleteTeamSpaceTask deleteTeamSpaceTask;

    @Override
    protected void doHandle(GroupDeletedEvent event) {
        TaskRunner.run(() -> deleteTeamSpaceTask.run(event.getCustomId()));
    }
}
