package com.ricky.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.fileextra.domain.event.FileExtraDeletedEvent;
import com.ricky.folder.domain.event.FolderCreatedEvent;
import com.ricky.folder.domain.event.FolderDeletedEvent;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import com.ricky.upload.domain.event.FileUploadedEvent;
import org.springframework.context.annotation.Configuration;

import static com.ricky.common.constants.ConfigConstants.*;

@Configuration
public class CommunityDomainEventJsonConfiguration implements DomainEventSubtypeRegistrar {

    @Override
    public void register(ObjectMapper mapper) {
        mapper.registerSubtypes(
                new NamedType(FilePublishedEvent.class, FILE_PUBLISHED_EVENT_NAME),
                new NamedType(FileWithdrewEvent.class, FILE_WITHDREW_EVENT_NAME)
        );
    }
}
