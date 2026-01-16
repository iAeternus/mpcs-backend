package com.ricky.publicfile.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileWithdrewEventHandler extends AbstractDomainEventHandler<FileWithdrewEvent> {
    @Override
    protected void doHandle(FileWithdrewEvent event) {
        // TODO 统计用户发布文件数
        // TODO 级联删除点赞
        // TODO 级联删除评论和评论层次结构
    }
}
