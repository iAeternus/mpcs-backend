package com.ricky.collaboration.collaboration.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.collaboration.collaboration.domain.ot.OperationTransformer;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.exception.*;
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
            throw new SessionAlreadyExistsException(documentId);
        }
        
        CollaborationSession session = new CollaborationSession(documentId, documentTitle, parentFolderId, userContext, 24);
        session.raiseSessionCreatedEvent(documentId, documentTitle, userContext);
        sessionRepository.save(session);
        
        log.info("Created collaboration session[{}] for document[{}]", session.getId(), documentId);
        return session;
    }
    
    public CollaborationSession getSession(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));
    }
    
    public Optional<CollaborationSession> getSessionOptional(String sessionId) {
        return sessionRepository.findById(sessionId);
    }
    
    public CollaborationSession getSessionByDocumentId(String documentId) {
        return sessionRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new SessionNotFoundException(documentId));
    }
    
    public CollaborationSession joinSession(String sessionId, UserContext userContext) {
        CollaborationSession session = getSession(sessionId);
        
        if (session.isExpired()) {
            throw new SessionExpiredException(sessionId);
        }
        
        if (session.isUserInSession(userContext.getUid())) {
            log.info("User[{}] already in session[{}]", userContext.getUid(), sessionId);
            return session;
        }
        
        boolean joined = session.join(userContext);
        if (!joined) {
            throw new SessionFullException();
        }
        
        session.raiseUserJoinedEvent(sessionId, userContext.getUid(), userContext.getUsername(), userContext);
        sessionRepository.save(session);
        
        log.info("User[{}] joined session[{}]", userContext.getUid(), sessionId);
        return session;
    }
    
    public CollaborationSession leaveSession(String sessionId, UserContext userContext) {
        CollaborationSession session = getSession(sessionId);
        
        session.leave(userContext.getUid(), userContext);
        session.raiseUserLeftEvent(sessionId, userContext.getUid(), userContext);
        sessionRepository.save(session);
        
        if (session.isEmpty()) {
            deleteSession(sessionId, userContext);
        }
        
        log.info("User[{}] left session[{}]", userContext.getUid(), sessionId);
        return session;
    }
    
    public void deleteSession(String sessionId, UserContext userContext) {
        CollaborationSession session = getSession(sessionId);
        session.raiseSessionDeletedEvent(sessionId, userContext);
        sessionRepository.delete(session);
        
        log.info("Deleted collaboration session[{}]", sessionId);
    }
    
    public CollaborationSession submitOperation(String sessionId, TextOperation operation, UserContext userContext) {
        CollaborationSession session = getSession(sessionId);
        
        if (!session.isUserInSession(userContext.getUid())) {
            throw new UserNotInSessionException(userContext.getUid(), sessionId);
        }
        
        if (session.isExpired()) {
            throw new SessionExpiredException(sessionId);
        }
        
        List<TextOperation> serverOps = session.getRecentOperations(100);
        TextOperation transformedOp = operation;
        
        if (!serverOps.isEmpty()) {
            transformedOp = operationTransformer.transform(operation, serverOps.get(serverOps.size() - 1));
        }
        
        session.addOperation(transformedOp, userContext);
        sessionRepository.save(session);
        
        log.debug("User[{}] submitted operation at session[{}]", userContext.getUid(), sessionId);
        return session;
    }
    
    public CollaborationSession updateCursor(String sessionId, String oderId, CursorPosition cursor, UserContext userContext) {
        CollaborationSession session = getSession(sessionId);
        
        if (!session.isUserInSession(oderId)) {
            throw new UserNotInSessionException(oderId, sessionId);
        }
        
        session.updateCursor(oderId, cursor, userContext);
        sessionRepository.save(session);
        
        return session;
    }
    
    public List<CollaborationSession> getUserSessions(String oderId) {
        return sessionRepository.findByUserId(oderId);
    }
    
    public void cleanupExpiredSessions() {
        sessionRepository.deleteExpiredSessions();
        log.info("Cleaned up expired collaboration sessions");
    }
}
