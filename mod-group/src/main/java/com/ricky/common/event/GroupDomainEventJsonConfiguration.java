package com.ricky.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.group.domain.event.GroupDeletedEvent;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import org.springframework.context.annotation.Configuration;

import static com.ricky.common.constants.ConfigConstants.GROUP_DELETED_EVENT_NAME;
import static com.ricky.common.constants.ConfigConstants.GROUP_MEMBERS_CHANGED_EVENT_NAME;

@Configuration
public class GroupDomainEventJsonConfiguration implements DomainEventSubtypeRegistrar {

    @Override
    public void register(ObjectMapper mapper) {
        mapper.registerSubtypes(
                new NamedType(GroupDeletedEvent.class, GROUP_DELETED_EVENT_NAME),
                new NamedType(GroupMembersChangedEvent.class, GROUP_MEMBERS_CHANGED_EVENT_NAME)
        );
    }
}
