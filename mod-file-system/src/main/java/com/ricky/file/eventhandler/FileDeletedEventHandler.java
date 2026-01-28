package com.ricky.file.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.file.domain.event.FileDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileDeletedEventHandler extends AbstractDomainEventHandler<FileDeletedEvent> {

    @Override
    protected void doHandle(FileDeletedEvent event) {
        // TODO 用户数据统计更新
    }
}
