package com.ricky.like.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.UNLIKED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.UNLIKED;

@Getter
@TypeAlias(UNLIKED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnlikedEvent extends DomainEvent {

    private String userId;
    private String postId;

    public UnlikedEvent(String userId, String postId, UserContext userContext) {
        super(UNLIKED, userContext);
        this.userId = userId;
        this.postId = postId;
    }
}
