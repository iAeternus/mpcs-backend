package com.ricky.collaboration.collaboration.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ricky.common.event.DomainEventType.COLLAB_SESSION_DELETED;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class SessionDeletedEvent extends DomainEvent {
    
    private String sessionId;
    
    public SessionDeletedEvent(String sessionId, UserContext userContext) {
        super(COLLAB_SESSION_DELETED, userContext);
        this.sessionId = sessionId;
    }
}
