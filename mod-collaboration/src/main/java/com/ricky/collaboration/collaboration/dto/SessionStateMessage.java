package com.ricky.collaboration.collaboration.dto;

import com.ricky.collaboration.collaboration.domain.CollabUser;
import com.ricky.collaboration.collaboration.domain.CursorPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStateMessage {
    
    private String type;
    private String sessionId;
    private long version;
    private List<CollabUser> activeUsers;
    private Map<String, CursorPosition> cursors;
    
    public static SessionStateMessage of(String sessionId, long version, 
                                          List<CollabUser> users, Map<String, CursorPosition> cursors) {
        return SessionStateMessage.builder()
                .type("session_state")
                .sessionId(sessionId)
                .version(version)
                .activeUsers(users)
                .cursors(cursors)
                .build();
    }
}
