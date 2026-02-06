package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.CommentDeletedLocalEvent;
import com.ricky.common.event.local.AbstractLocalDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentDeletedLocalEventHandler extends AbstractLocalDomainEventHandler<CommentDeletedLocalEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(CommentDeletedLocalEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), -1);
    }
}
