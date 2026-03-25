package com.ricky.collaboration.domain.event;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ricky.common.constants.ConfigConstants.COLLAB_SESSION_ID_PREFIX;
import static com.ricky.common.event.DomainEventType.COLLAB_SESSION_CREATED;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class SessionCreatedEvent extends DomainEvent {

    private String sessionId;
    private String documentId;
    private String documentTitle;

    public SessionCreatedEvent(String sessionId, String documentId, String documentTitle, UserContext userContext) {
        super(COLLAB_SESSION_CREATED, userContext);
        this.sessionId = sessionId;
        this.documentId = documentId;
        this.documentTitle = documentTitle;
    }

    public static String newEventId() {
        return COLLAB_SESSION_ID_PREFIX + "EVT" + System.currentTimeMillis();
    }
}
