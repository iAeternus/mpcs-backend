package com.ricky.collaboration.service.impl;

import com.ricky.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.domain.CollaborationDomainService;
import com.ricky.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.domain.CollaborationSessionRepository;
import com.ricky.collaboration.domain.CursorPosition;
import com.ricky.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.query.SessionInfoResponse;
import com.ricky.collaboration.service.CollaborationService;
import com.ricky.lock.service.EditingLockService;
import com.ricky.common.domain.user.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {

    private final CollaborationDomainService domainService;
    private final CollaborationSessionRepository sessionRepository;
    private final EditingLockService editingLockService;

    @Override
    @Transactional
    public SessionInfoResponse createSession(CreateSessionCommand command, UserContext userContext) {
        CollaborationSession session = domainService.createSession(
                command.getDocumentId(),
                command.getDocumentTitle(),
                command.getParentFolderId(),
                userContext
        );
        sessionRepository.save(session);
        return SessionInfoResponse.fromSession(session);
    }

    @Override
    public SessionInfoResponse getSessionInfo(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        return SessionInfoResponse.fromSession(session);
    }

    @Override
    public SessionInfoResponse getSessionByDocumentId(String documentId, UserContext userContext) {
        CollaborationSession session = domainService.getSessionByDocumentId(documentId);
        return SessionInfoResponse.fromSession(session);
    }

    @Override
    @Transactional
    public SessionInfoResponse joinSession(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        boolean canJoin = domainService.canJoinSession(session, userContext.getUid());

        if (canJoin) {
            domainService.joinSession(session, userContext);
            sessionRepository.save(session);
        }

        return SessionInfoResponse.fromSession(session);
    }

    @Override
    @Transactional
    public void leaveSession(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        domainService.leaveSession(session, userContext);
        sessionRepository.save(session);
        editingLockService.releaseUserLocks(sessionId, userContext.getUid(), userContext);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        domainService.deleteSession(session, sessionId, userContext);
        sessionRepository.delete(session);
    }

    @Override
    @Transactional
    public SessionInfoResponse submitOperation(SubmitOperationCommand command, UserContext userContext) {
        TextOperation operation = TextOperation.buildOperation(command, userContext);

        CollaborationSession session = domainService.getSession(command.getSessionId());
        domainService.validateUserInSession(session, userContext.getUid());
        domainService.validateSessionNotExpired(session);
        editingLockService.validateOperationAllowed(session.getId(), operation, userContext.getUid());

        List<TextOperation> serverOps = session.getOperationsSince(operation.getClientVersion());
        TextOperation transformedOp = domainService.transformOperation(operation, serverOps);

        domainService.addOperation(session, transformedOp, userContext);
        editingLockService.rebaseLocks(session.getId(), session.getDocumentId(), transformedOp, userContext.getUid(), userContext);
        sessionRepository.save(session);

        return SessionInfoResponse.fromSession(session);
    }

    @Override
    @Transactional
    public SessionInfoResponse updateCursor(UpdateCursorCommand command, UserContext userContext) {
        CursorPosition cursor = CursorPosition.of(
                userContext.getUid(),
                userContext.getUsername(),
                command.getPosition(),
                command.getSelectionStart() != null ? command.getSelectionStart() : command.getPosition(),
                command.getSelectionEnd() != null ? command.getSelectionEnd() : command.getPosition()
        );

        CollaborationSession session = domainService.getSession(command.getSessionId());
        domainService.validateUserInSession(session, userContext.getUid());

        domainService.updateCursor(session, userContext.getUid(), cursor, userContext);
        sessionRepository.save(session);

        return SessionInfoResponse.fromSession(session);
    }

    @Override
    public OperationHistoryResponse getOperationHistory(String sessionId, long fromVersion, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        List<TextOperation> operations = session.getOperationsSince(fromVersion);

        log.info("getOperationHistory: sessionId={}, fromVersion={}, operationsCount={}, operations={}",
                sessionId, fromVersion, operations.size(), operations);

        return OperationHistoryResponse.builder()
                .sessionId(sessionId)
                .fromVersion(fromVersion)
                .toVersion(session.getVersion().getVersion())
                .operations(operations)
                .build();
    }

    @Override
    @Transactional
    public CollaborationSession submitOperationInternal(String sessionId, TextOperation operation, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        domainService.validateUserInSession(session, userContext.getUid());
        domainService.validateSessionNotExpired(session);
        editingLockService.validateOperationAllowed(session.getId(), operation, userContext.getUid());

        List<TextOperation> serverOps = session.getOperationsSince(operation.getClientVersion());
        TextOperation transformedOp = domainService.transformOperation(operation, serverOps);

        domainService.addOperation(session, transformedOp, userContext);
        editingLockService.rebaseLocks(session.getId(), session.getDocumentId(), transformedOp, userContext.getUid(), userContext);
        sessionRepository.save(session);

        return session;
    }

    @Override
    @Transactional
    public SessionInfoResponse updateBaseVersion(String sessionId, long baseVersion, UserContext userContext) {
        log.info("updateBaseVersion called: sessionId={}, baseVersion={}, userId={}", sessionId, baseVersion, userContext.getUid());

        CollaborationSession session = domainService.getSession(sessionId);
        domainService.validateUserInSession(session, userContext.getUid());
        domainService.validateSessionNotExpired(session);

        log.info("Session found, user in session. Current baseVersion={}, new baseVersion={}",
                session.getBaseVersion(), baseVersion);

        session.updateBaseVersion(baseVersion, userContext);
        sessionRepository.save(session);

        return SessionInfoResponse.fromSession(session);
    }
}
