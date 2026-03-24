package com.ricky.collaboration.collaboration.query;

import com.ricky.collaboration.collaboration.domain.CollabUser;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.CursorPosition;
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
    private String parentFolderId;
    private long version;
    private long documentLength;
    private long baseVersion;
    private int activeUserCount;
    private List<CollabUser> activeUsers;
    private Map<String, CursorPosition> cursors;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean expired;

    public long getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(long baseVersion) {
        this.baseVersion = baseVersion;
    }

    public static SessionInfoResponse fromSession(CollaborationSession session) {
        return SessionInfoResponse.builder()
                .sessionId(session.getId())
                .documentId(session.getDocumentId())
                .documentTitle(session.getDocumentTitle())
                .parentFolderId(session.getParentFolderId())
                .version(session.getVersion().getVersion())
                .documentLength(session.getVersion().getDocumentLength())
                .baseVersion(session.getBaseVersion() != null ? session.getBaseVersion() : 0L)
                .activeUserCount(session.getActiveUserCount())
                .activeUsers(List.copyOf(session.getActiveUsers()))
                .cursors(session.getCursors())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .expired(session.isExpired())
                .build();
    }
}
