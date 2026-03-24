package com.ricky.collaboration.collaboration.service;

import com.ricky.collaboration.collaboration.command.CreateSessionCommand;
import com.ricky.collaboration.collaboration.command.SubmitOperationCommand;
import com.ricky.collaboration.collaboration.command.UpdateCursorCommand;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.query.OperationHistoryResponse;
import com.ricky.collaboration.collaboration.query.SessionInfoResponse;
import com.ricky.common.domain.user.UserContext;

public interface CollaborationService {

    SessionInfoResponse createSession(CreateSessionCommand command, UserContext userContext);

    SessionInfoResponse getSessionInfo(String sessionId, UserContext userContext);

    SessionInfoResponse getSessionByDocumentId(String documentId, UserContext userContext);

    SessionInfoResponse joinSession(String sessionId, UserContext userContext);

    void leaveSession(String sessionId, UserContext userContext);

    void deleteSession(String sessionId, UserContext userContext);

    SessionInfoResponse submitOperation(SubmitOperationCommand command, UserContext userContext);

    SessionInfoResponse updateCursor(UpdateCursorCommand command, UserContext userContext);

    OperationHistoryResponse getOperationHistory(String sessionId, long fromVersion, UserContext userContext);

    CollaborationSession submitOperationInternal(String sessionId, TextOperation operation, UserContext userContext);

    SessionInfoResponse updateBaseVersion(String sessionId, long baseVersion, UserContext userContext);
}
