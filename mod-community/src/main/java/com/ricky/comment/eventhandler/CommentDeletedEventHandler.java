package com.ricky.comment.eventhandler;

import com.ricky.comment.domain.CachedCommentRepository;
import com.ricky.comment.domain.event.CommentDeletedEvent;
import com.ricky.common.event.consume.AbstractDomainEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentDeletedEventHandler extends AbstractDomainEventHandler<CommentDeletedEvent> {

    private final CachedCommentRepository cachedCommentRepository;

    @Override
    protected void doHandle(CommentDeletedEvent event) {
        cachedCommentRepository.increaseCommentCount(event.getPostId(), -1);
    }
}
