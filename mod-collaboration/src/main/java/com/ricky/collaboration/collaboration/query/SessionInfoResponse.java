package com.ricky.collaboration.collaboration.query;

import com.ricky.collaboration.collaboration.domain.CollabUser;
import com.ricky.collaboration.collaboration.domain.CursorPosition;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoResponse {
    
    private String sessionId;
    private String documentId;
    private String documentTitle;
    private long version;
    private long documentLength;
    private int activeUserCount;
    private List<CollabUser> activeUsers;
    private Map<String, CursorPosition> cursors;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean expired;
}
