package com.ricky.publicfile.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.common.utils.TaskRunner;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.task.SensitiveWordTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilePublishedEventHandler extends AbstractDomainEventHandler<FilePublishedEvent> {

    private final SensitiveWordTask sensitiveWordTask;

    @Override
    protected void doHandle(FilePublishedEvent event) {
        TaskRunner.run(() -> sensitiveWordTask.run(event.getOriginalFileId(), event.getPostId()));
        // TODO 统计用户发布文件数
    }
}
