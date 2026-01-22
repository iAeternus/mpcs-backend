package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.CommentCreatedEvent;
import com.ricky.common.event.consume.AbstractDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentCreatedEventHandler extends AbstractDomainEventHandler<CommentCreatedEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(CommentCreatedEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), 1);
    }
}
