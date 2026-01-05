package com.ricky.upload.handler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.upload.domain.evt.FileUploadedEvent;
import com.ricky.upload.handler.tasks.SyncFileToEsTask;
import com.ricky.upload.handler.tasks.extracttext.ExtractTextTask;
import com.ricky.upload.handler.tasks.summary.GenerateSummaryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUploadedEventHandler extends AbstractDomainEventHandler<FileUploadedEvent> {

    private final ExtractTextTask extractTextTask;
    private final SyncFileToEsTask syncFileToEsTask;
    private final GenerateSummaryTask generateSummaryTask;

    // 执行顺序：extractTextTask -> generateSummaryTask -> syncFileToEsTask
    @Override
    protected void doHandle(FileUploadedEvent evt) {
        TaskRunner.run(() -> extractTextTask.run(evt.getFileId(), evt.getStorageId(), evt.getCategory()));
        TaskRunner.run(() -> generateSummaryTask.run(evt.getFileId()));
        TaskRunner.run(() -> syncFileToEsTask.run(evt.getFileId()));
    }
}
