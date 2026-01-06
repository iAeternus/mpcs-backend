package com.ricky.fileextra.handler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.fileextra.domain.evt.FileExtraDeletedEvent;
import com.ricky.fileextra.handler.tasks.DeleteTextFileTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileExtraDeletedEventHandler extends AbstractDomainEventHandler<FileExtraDeletedEvent> {

    private final DeleteTextFileTask deleteTextFileTask;

    @Override
    protected void doHandle(FileExtraDeletedEvent evt) {
        TaskRunner.run(() -> deleteTextFileTask.run(evt.getTextFilePath()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
