package com.ricky.collaboration.collaboration.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.CollaborationDomainService;
import com.ricky.collaboration.collaboration.domain.CollabUser;
import com.ricky.collaboration.collaboration.domain.CursorPosition;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import com.ricky.collaboration.collaboration.exception.OperationInvalidException;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {
    
    private final CollaborationDomainService domainService;
    
    @Override
    public SessionInfoResponse createSession(CreateSessionCommand command, UserContext userContext) {
        CollaborationSession session = domainService.createSession(
                command.getDocumentId(),
                command.getDocumentTitle(),
                userContext
        );
        return toSessionInfoResponse(session);
    }
    
    @Override
    public SessionInfoResponse getSessionInfo(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        return toSessionInfoResponse(session);
    }
    
    @Override
    public SessionInfoResponse joinSession(String sessionId, UserContext userContext) {
        CollaborationSession session = domainService.joinSession(sessionId, userContext);
        return toSessionInfoResponse(session);
    }
    
    @Override
    public void leaveSession(String sessionId, UserContext userContext) {
        domainService.leaveSession(sessionId, userContext);
    }
    
    @Override
    public void deleteSession(String sessionId, UserContext userContext) {
        domainService.deleteSession(sessionId, userContext);
    }
    
    @Override
    public SessionInfoResponse submitOperation(SubmitOperationCommand command, UserContext userContext) {
        TextOperation operation = buildOperation(command, userContext);
        CollaborationSession session = domainService.submitOperation(command.getSessionId(), operation, userContext);
        return toSessionInfoResponse(session);
    }
    
    @Override
    public SessionInfoResponse updateCursor(UpdateCursorCommand command, UserContext userContext) {
        CursorPosition cursor = CursorPosition.of(
                userContext.getUid(),
                userContext.getUsername(),
                command.getPosition(),
                command.getSelectionStart() != null ? command.getSelectionStart() : command.getPosition(),
                command.getSelectionEnd() != null ? command.getSelectionEnd() : command.getPosition()
        );
        CollaborationSession session = domainService.updateCursor(
                command.getSessionId(),
                userContext.getUid(),
                cursor,
                userContext
        );
        return toSessionInfoResponse(session);
    }
    
    @Override
    public OperationHistoryResponse getOperationHistory(String sessionId, long fromVersion, UserContext userContext) {
        CollaborationSession session = domainService.getSession(sessionId);
        List<TextOperation> operations = session.getOperationsSince(fromVersion);
        
        return OperationHistoryResponse.builder()
                .sessionId(sessionId)
                .fromVersion(fromVersion)
                .toVersion(session.getVersion().getVersion())
                .operations(operations)
                .build();
    }
    
    @Override
    public CollaborationSession submitOperationInternal(String sessionId, TextOperation operation, UserContext userContext) {
        return domainService.submitOperation(sessionId, operation, userContext);
    }
    
    private TextOperation buildOperation(SubmitOperationCommand command, UserContext userContext) {
        TextOperationType type = command.getType();
        
        return switch (type) {
            case INSERT -> {
                if (command.getContent() == null || command.getContent().isEmpty()) {
                    throw new OperationInvalidException("INSERT操作必须提供content");
                }
                yield TextOperation.insert(
                        userContext.getUid(),
                        command.getPosition(),
                        command.getContent(),
                        command.getClientVersion()
                );
            }
            case DELETE -> {
                if (command.getLength() == null || command.getLength() <= 0) {
                    throw new OperationInvalidException("DELETE操作必须提供有效的length");
                }
                yield TextOperation.delete(
                        userContext.getUid(),
                        command.getPosition(),
                        command.getLength(),
                        command.getClientVersion()
                );
            }
            case RETAIN -> TextOperation.retain(
                    userContext.getUid(),
                    command.getPosition(),
                    command.getLength() != null ? command.getLength() : 0,
                    command.getClientVersion()
            );
        };
    }
    
    private SessionInfoResponse toSessionInfoResponse(CollaborationSession session) {
        return SessionInfoResponse.builder()
                .sessionId(session.getId())
                .documentId(session.getDocumentId())
                .documentTitle(session.getDocumentTitle())
                .version(session.getVersion().getVersion())
                .documentLength(session.getVersion().getDocumentLength())
                .activeUserCount(session.getActiveUserCount())
                .activeUsers(List.copyOf(session.getActiveUsers()))
                .cursors(session.getCursors())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .expired(session.isExpired())
                .build();
    }
}
