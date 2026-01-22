package com.ricky.comment.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.COMMENT_CREATED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.COMMENT_CREATED;

@Getter
@TypeAlias(COMMENT_CREATED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentCreatedEvent extends DomainEvent {

    private String postId;

    public CommentCreatedEvent(String postId, UserContext userContext) {
        super(COMMENT_CREATED, userContext);
        this.postId = postId;
    }
}
