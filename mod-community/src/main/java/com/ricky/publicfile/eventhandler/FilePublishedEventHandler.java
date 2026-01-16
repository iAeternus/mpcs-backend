package com.ricky.publicfile.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FilePublishedEventHandler extends AbstractDomainEventHandler<FilePublishedEvent> {
    @Override
    protected void doHandle(FilePublishedEvent event) {
        // TODO 统计用户发布文件数
        // TODO 敏感词检测 -- 维护 status
    }
}
