package com.ricky.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import org.springframework.context.annotation.Configuration;

import static com.ricky.common.constants.ConfigConstants.*;

@Configuration
public class CommunityDomainEventJsonConfiguration implements DomainEventSubtypeRegistrar {

    @Override
    public void register(ObjectMapper mapper) {
        mapper.registerSubtypes(
                new NamedType(FilePublishedEvent.class, FILE_PUBLISHED_EVENT_NAME),
                new NamedType(FileWithdrewEvent.class, FILE_WITHDREW_EVENT_NAME),
                new NamedType(FileWithdrewEvent.class, COMMENT_CREATED_EVENT_NAME),
                new NamedType(FileWithdrewEvent.class, COMMENT_DELETED_EVENT_NAME)
        );
    }
}
