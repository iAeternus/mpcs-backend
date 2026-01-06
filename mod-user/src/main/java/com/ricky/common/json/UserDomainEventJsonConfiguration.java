package com.ricky.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.common.event.DomainEventSubtypeRegistrar;
import com.ricky.user.domain.event.UserCreatedEvent;
import org.springframework.context.annotation.Configuration;

import static com.ricky.common.constants.ConfigConstants.USER_CREATED_EVENT_NAME;

@Configuration
public class UserDomainEventJsonConfiguration implements DomainEventSubtypeRegistrar {

    @Override
    public void register(ObjectMapper mapper) {
        mapper.registerSubtypes(
                new NamedType(UserCreatedEvent.class, USER_CREATED_EVENT_NAME)
        );
    }
}
