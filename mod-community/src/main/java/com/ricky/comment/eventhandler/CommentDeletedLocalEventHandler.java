package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.LocalCommentDeletedEvent;
import com.ricky.common.event.LocalDomainEvent;
import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentDeletedLocalEventHandler extends AbstractLocalDomainEventHandler<LocalCommentDeletedEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(LocalCommentDeletedEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), -1);
    }

    @Override
    public boolean supports(LocalDomainEvent event) {
        return event instanceof LocalCommentDeletedEvent;
    }
}
