package com.ricky.file.handler;

import com.ricky.common.domain.event.DomainEvent;
import com.ricky.common.domain.event.DomainEventHandler;
import com.ricky.common.domain.event.DomainEventTypeEnum;
import com.ricky.common.utils.TaskRunner;
import com.ricky.file.domain.evt.FileUploadedEvent;
import com.ricky.file.handler.tasks.SyncFileToEsTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUploadedEventHandler implements DomainEventHandler {

    private final SyncFileToEsTask syncFileToEsTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DomainEventTypeEnum.FILE_UPLOADED_EVENT;
    }

    @Override
    public void handle(DomainEvent domainEvent, TaskRunner taskRunner) {
        FileUploadedEvent evt = (FileUploadedEvent) domainEvent;

        taskRunner.run(() -> syncFileToEsTask.run(
                evt.getFileId(),
                evt.getFilename(),
                evt.getHash(),
                evt.getSize(),
                evt.getMimeType()
        ));
    }
}
