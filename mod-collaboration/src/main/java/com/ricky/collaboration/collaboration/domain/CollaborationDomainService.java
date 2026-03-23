package com.ricky.collaboration.collaboration.domain;

import com.ricky.collaboration.collaboration.domain.ot.OperationTransformer;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.ErrorCodeEnum;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationDomainService {

    private final CollaborationSessionRepository sessionRepository;
    private final OperationTransformer operationTransformer;

    public CollaborationSession createSession(String documentId, String documentTitle, String parentFolderId, UserContext userContext) {
        if (sessionRepository.existsByDocumentId(documentId)) {
            throw MyException.requestValidationException("documentId", documentId);
        }

        CollaborationSession session = new CollaborationSession(documentId, documentTitle, parentFolderId, userContext, 24);
        session.raiseSessionCreatedEvent(documentId, documentTitle, userContext);

        log.info("Created collaboration session[{}] for document[{}]", session.getId(), documentId);
        return session;
    }

    public void validateSessionExists(String sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new MyException(ErrorCodeEnum.COLLAB_SESSION_NOT_FOUND, "协同会话不存在。", "sessionId", sessionId);
        }
    }

    public CollaborationSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new MyException(ErrorCodeEnum.COLLAB_SESSION_NOT_FOUND, "协同会话不存在。", "sessionId", sessionId));
    }

    public Optional<CollaborationSession> getSessionOptional(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public CollaborationSession getSessionByDocumentId(String documentId) {
        return sessionRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new MyException(ErrorCodeEnum.COLLAB_SESSION_NOT_FOUND, "协同会话不存在。", "documentId", documentId));
    }

    public boolean canJoinSession(CollaborationSession session, String oderId) {
        if (session.isExpired()) {
            throw new MyException(ErrorCodeEnum.COLLAB_SESSION_EXPIRED, "协同会话已过期。", "sessionId", session.getId());
        }

        if (session.isUserInSession(oderId)) {
            log.info("User[{}] already in session[{}]", oderId, session.getId());
            return false;
        }

        if (session.isFull()) {
            throw new MyException(ErrorCodeEnum.COLLAB_SESSION_FULL, "协同会话已满。");
        }

        return true;
    }

    public void joinSession(CollaborationSession session, UserContext userContext) {
        session.join(userContext);
        session.raiseUserJoinedEvent(session.getId(), userContext.getUid(), userContext.getUsername(), userContext);
        log.info("User[{}] joined session[{}]", userContext.getUid(), session.getId());
    }

    public void leaveSession(CollaborationSession session, UserContext userContext) {
        session.leave(userContext.getUid(), userContext);
        session.raiseUserLeftEvent(session.getId(), userContext.getUid(), userContext);
        log.info("User[{}] left session[{}]", userContext.getUid(), session.getId());
    }

    public void deleteSession(CollaborationSession session, String sessionId, UserContext userContext) {
        session.raiseSessionDeletedEvent(sessionId, userContext);
        log.info("Deleted collaboration session[{}]", sessionId);
    }

    public void validateUserInSession(CollaborationSession session, String oderId) {
        if (!session.isUserInSession(oderId)) {
            throw new MyException(ErrorCodeEnum.COLLAB_USER_NOT_IN_SESSION, "用户不在协同会话中。", "userId", oderId, "sessionId", session.getId());
        }
    }

    public void validateSessionNotExpired(CollaborationSession session) {
        if (session.isExpired()) {
            throw new MyException(ErrorCodeEnum.COLLAB_SESSION_EXPIRED, "协同会话已过期。", "sessionId", session.getId());
        }
    }

    public TextOperation transformOperation(TextOperation operation, List<TextOperation> serverOps) {
        if (serverOps.isEmpty()) {
            return operation;
        }
        return operationTransformer.transform(operation, serverOps.get(serverOps.size() - 1));
    }

    public void addOperation(CollaborationSession session, TextOperation operation, UserContext userContext) {
        session.addOperation(operation, userContext);
        log.debug("User[{}] submitted operation at session[{}]", userContext.getUid(), session.getId());
    }

    public void updateCursor(CollaborationSession session, String oderId, CursorPosition cursor, UserContext userContext) {
        session.updateCursor(oderId, cursor, userContext);
    }

    public List<CollaborationSession> getUserSessions(String oderId) {
        return sessionRepository.findByUserId(oderId);
    }

    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
        log.info("Cleaned up expired collaboration sessions");
    }
}
