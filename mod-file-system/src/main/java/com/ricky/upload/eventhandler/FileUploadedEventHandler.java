package com.ricky.upload.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.upload.domain.event.FileUploadedEvent;
import com.ricky.upload.domain.tasks.SyncFileToEsTask;
import com.ricky.upload.domain.tasks.extracttext.ExtractTextTask;
import com.ricky.upload.domain.tasks.summary.GenerateSummaryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUploadedEventHandler extends AbstractDomainEventHandler<FileUploadedEvent> {

    private final ExtractTextTask extractTextTask;
    private final SyncFileToEsTask syncFileToEsTask;
    private final GenerateSummaryTask generateSummaryTask;

    // 执行顺序：
    // 1, extractTextTask
    // 2. generateSummaryTask/syncFileToEsTask
    @Override
    protected void doHandle(FileUploadedEvent evt) {
        TaskRunner.run(() -> extractTextTask.run(evt.getFileId(), evt.getStorageId(), evt.getCategory()));
        TaskRunner.run(() -> generateSummaryTask.run(evt.getFileId()));
        TaskRunner.run(() -> syncFileToEsTask.run(evt.getFileId()));
    }
}
