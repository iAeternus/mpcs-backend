package com.ricky.collaboration.collaboration.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ricky.common.event.DomainEventType.COLLAB_USER_JOINED;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserJoinedEvent extends DomainEvent {
    
    private String sessionId;
    private String oderId;
    private String username;
    
    public UserJoinedEvent(String sessionId, String oderId, String username, UserContext userContext) {
        super(COLLAB_USER_JOINED, userContext);
        this.sessionId = sessionId;
        this.oderId = oderId;
        this.username = username;
    }
}
