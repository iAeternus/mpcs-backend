package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.LocalCommentCreatedEvent;
import com.ricky.common.event.LocalDomainEvent;
import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentCreatedLocalEventHandler extends AbstractLocalDomainEventHandler<LocalCommentCreatedEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(LocalCommentCreatedEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), 1);
    }

    @Override
    public boolean supports(LocalDomainEvent event) {
        return event instanceof LocalCommentCreatedEvent;
    }

}
