package com.ricky.like.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.ricky.common.constants.ConfigConstants.LIKED_EVENT_NAME;
import static com.ricky.common.event.DomainEventType.LIKED;

@Getter
@TypeAlias(LIKED_EVENT_NAME)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LikedEvent extends DomainEvent {

    private String userId;
    private String postId;

    public LikedEvent(String userId, String postId, UserContext userContext) {
        super(LIKED, userContext);
        this.userId = userId;
        this.postId = postId;
    }

}
