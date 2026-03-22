package com.ricky.collaboration.collaboration.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ricky.common.event.DomainEventType.COLLAB_USER_LEFT;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserLeftEvent extends DomainEvent {
    
    private String sessionId;
    private String oderId;
    
    public UserLeftEvent(String sessionId, String oderId, UserContext userContext) {
        super(COLLAB_USER_LEFT, userContext);
        this.sessionId = sessionId;
        this.oderId = oderId;
    }
}
