package com.ricky.comment.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.COMMENT_DELETED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.COMMENT_DELETED;

@Getter
@TypeAlias(COMMENT_DELETED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentDeletedEvent extends DomainEvent {

    private String postId;

    public CommentDeletedEvent(String postId, UserContext userContext) {
        super(COMMENT_DELETED, userContext);
        this.postId = postId;
    }
}
