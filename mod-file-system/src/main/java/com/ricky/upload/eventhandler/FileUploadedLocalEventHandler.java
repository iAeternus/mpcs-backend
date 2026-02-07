package com.ricky.upload.eventhandler;

import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.upload.domain.event.FileUploadedLocalEvent;
import com.ricky.upload.domain.tasks.SyncFileToEsTask;
import com.ricky.upload.domain.tasks.extracttext.ExtractTextTask;
import com.ricky.upload.domain.tasks.summary.GenerateSummaryTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUploadedLocalEventHandler extends AbstractLocalDomainEventHandler<FileUploadedLocalEvent> {

    private final ExtractTextTask extractTextTask;
    private final SyncFileToEsTask syncFileToEsTask;
    private final GenerateSummaryTask generateSummaryTask;

    // 执行顺序：
    // 1, extractTextTask
    // 2. generateSummaryTask/syncFileToEsTask
    @Override
    protected void doHandle(FileUploadedLocalEvent evt) {
        TaskRunner.run("extractTextTask", () -> extractTextTask.run(evt.getFileId(), evt.getStorageId(), evt.getCategory()));
        TaskRunner.run("generateSummaryTask", () -> generateSummaryTask.run(evt.getFileId()));
        TaskRunner.run("syncFileToEsTask", () -> syncFileToEsTask.run(evt.getFileId()));
    }
}
