package com.ricky.collaboration.domain;

import java.util.List;
import java.util.Optional;

public interface CollaborationSessionRepository {

    void save(CollaborationSession session);

    void delete(CollaborationSession session);

    Optional<CollaborationSession> findById(String sessionId);

    Optional<CollaborationSession> findByDocumentId(String documentId);

    List<CollaborationSession> findByUserId(String oderId);

    List<CollaborationSession> findActiveSessions();

    boolean existsById(String sessionId);

    boolean existsByDocumentId(String documentId);

    void deleteExpiredSessions();
}
