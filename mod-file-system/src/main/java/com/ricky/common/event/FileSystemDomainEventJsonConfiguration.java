package com.ricky.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.fileextra.domain.event.FileExtraDeletedEvent;
import com.ricky.folder.domain.event.FolderCreatedEvent;
import com.ricky.folder.domain.event.FolderDeletedEvent;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import com.ricky.upload.domain.event.FileUploadedEvent;
import org.springframework.context.annotation.Configuration;

import static com.ricky.common.constants.ConfigConstants.*;

@Configuration
public class FileSystemDomainEventJsonConfiguration implements DomainEventSubtypeRegistrar {

    @Override
    public void register(ObjectMapper mapper) {
        mapper.registerSubtypes(
                new NamedType(FileUploadedEvent.class, FILE_DELETED_EVENT_NAME),
                new NamedType(FolderCreatedEvent.class, FOLDER_CREATED_EVENT_NAME),
                new NamedType(FolderDeletedEvent.class, FOLDER_DELETED_EVENT_NAME),
                new NamedType(FolderHierarchyChangedEvent.class, FOLDER_HIERARCHY_CHANGED_EVENT_NAME),
                new NamedType(FileUploadedEvent.class, FILE_UPLOADED_EVENT_NAME),
                new NamedType(FileExtraDeletedEvent.class, FILE_EXTRA_DELETED_EVENT_NAME)
        );
    }
}
