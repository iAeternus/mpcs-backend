package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.CommentCreatedLocalEvent;
import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentCreatedLocalEventHandler
        extends AbstractLocalDomainEventHandler<CommentCreatedLocalEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(CommentCreatedLocalEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), 1);
    }
}
