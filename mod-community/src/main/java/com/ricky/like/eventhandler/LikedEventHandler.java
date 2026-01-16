package com.ricky.like.eventhandler;

import com.ricky.common.event.consume.AbstractDomainEventHandler;
import com.ricky.like.domain.event.LikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikedEventHandler extends AbstractDomainEventHandler<LikedEvent> {
    @Override
    protected void doHandle(LikedEvent event) {
        // TODO 统计用户点赞数
        // TODO 统计PublicFile点赞数
    }
}
