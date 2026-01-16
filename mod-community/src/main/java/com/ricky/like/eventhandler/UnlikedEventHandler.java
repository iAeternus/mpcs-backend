package com.ricky.like.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.like.domain.event.UnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnlikedEventHandler extends AbstractDomainEventHandler<UnlikedEvent> {
    @Override
    protected void doHandle(UnlikedEvent event) {
        // TODO 统计用户点赞数
        // TODO 统计PublicFile点赞数
    }
}
