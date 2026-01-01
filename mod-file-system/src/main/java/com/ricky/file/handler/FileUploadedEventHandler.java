package com.ricky.file.handler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.file.domain.evt.FileUploadedEvent;
import com.ricky.file.handler.tasks.SyncFileToEsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadedEventHandler extends AbstractDomainEventHandler<FileUploadedEvent> {

    private final SyncFileToEsTask syncFileToEsTask;

    @Override
    protected void doHandle(FileUploadedEvent event) {
        TaskRunner.run(() -> syncFileToEsTask.run(
                event.getFileId(),
                event.getFilename(),
                event.getHash(),
                event.getSize()
        ));
    }
}
