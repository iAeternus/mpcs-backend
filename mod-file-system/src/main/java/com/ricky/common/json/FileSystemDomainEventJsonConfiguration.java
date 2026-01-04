package com.ricky.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.ricky.common.event.DomainEventSubtypeRegistrar;
import com.ricky.folder.domain.evt.FolderCreatedEvent;
import com.ricky.folder.domain.evt.FolderDeletedEvent;
import com.ricky.folder.domain.evt.FolderRenamedEvent;
import com.ricky.folderhierarchy.domain.evt.FolderHierarchyChangedEvent;
import com.ricky.upload.domain.evt.FileUploadedEvent;
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
                new NamedType(FolderRenamedEvent.class, FOLDER_RENAMED_EVENT_NAME),
                new NamedType(FolderHierarchyChangedEvent.class, FOLDER_HIERARCHY_CHANGED_EVENT_NAME),
                new NamedType(FileUploadedEvent.class, FILE_UPLOADED_EVENT_NAME)
        );
    }
}
