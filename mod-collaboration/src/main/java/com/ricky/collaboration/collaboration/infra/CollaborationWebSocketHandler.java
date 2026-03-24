package com.ricky.collaboration.collaboration.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricky.collaboration.collaboration.domain.CollaborationDomainService;
import com.ricky.collaboration.collaboration.domain.CollaborationSession;
import com.ricky.collaboration.collaboration.domain.CollaborationSessionRepository;
import com.ricky.collaboration.collaboration.domain.CursorPosition;
import com.ricky.collaboration.collaboration.domain.ot.TextOperation;
import com.ricky.collaboration.collaboration.domain.ot.TextOperationType;
import com.ricky.collaboration.collaboration.dto.CursorMessage;
import com.ricky.collaboration.collaboration.dto.OperationAckMessage;
import com.ricky.collaboration.collaboration.dto.OperationMessage;
import com.ricky.collaboration.collaboration.dto.SessionStateMessage;
import com.ricky.collaboration.lock.dto.EditingLockStateMessage;
import com.ricky.collaboration.lock.service.EditingLockService;
import com.ricky.common.domain.user.Role;
import com.ricky.common.domain.user.UserContext;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollaborationWebSocketHandler extends TextWebSocketHandler {

    private final CollaborationDomainService domainService;
    private final CollaborationSessionRepository sessionRepository;
    private final CollaborationSessionManager sessionManager;
    private final EditingLockService editingLockService;
    private final ObjectMapper objectMapper;

    private static final String SESSION_ID_ATTR = "sessionId";
    private static final String USER_ID_ATTR = "userId";
    private static final String USERNAME_ATTR = "username";

    @PostConstruct
    public void init() {
        log.info("CollaborationWebSocketHandler initialized successfully");
        log.info("  - domainService: {}", domainService != null ? "OK" : "NULL");
        log.info("  - sessionRepository: {}", sessionRepository != null ? "OK" : "NULL");
        log.info("  - sessionManager: {}", sessionManager != null ? "OK" : "NULL");
        log.info("  - objectMapper: {}", objectMapper != null ? "OK" : "NULL");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket afterConnectionEstablished, uri={}, id={}", session.getUri(), session.getId());

        try {
            String sessionId = extractSessionId(session);
            String oderId = extractUserId(session);
            String username = extractUsername(session);

            log.info("WebSocket: sessionId='{}', oderId='{}', username='{}'", sessionId, oderId, username);

            if (sessionId == null || oderId == null) {
                log.error("WebSocket: missing sessionId or oderId");
                session.close(CloseStatus.BAD_DATA.withReason("Missing sessionId or oderId"));
                return;
            }

            session.getAttributes().put(SESSION_ID_ATTR, sessionId);
            session.getAttributes().put(USER_ID_ATTR, oderId);
            session.getAttributes().put(USERNAME_ATTR, username);

            log.debug("Looking up collaboration session: {}", sessionId);
            CollaborationSession collabSession = domainService.getSessionOptional(sessionId)
                    .orElse(null);

            if (collabSession == null) {
                log.error("WebSocket: session not found: {}", sessionId);
                try {
                    sendMessage(session, OperationAckMessage.failure(sessionId, 0, "Session not found"));
                } catch (Exception e) {
                    log.error("Failed to send session not found message", e);
                }
                session.close(CloseStatus.GOING_AWAY.withReason("Session not found"));
                return;
            }

            log.debug("Session found, adding to session manager");
            sessionManager.addSession(sessionId, session);

            SessionStateMessage stateMessage = SessionStateMessage.of(
                    sessionId,
                    collabSession.getVersion().getVersion(),
                    collabSession.getActiveUsers().stream().toList(),
                    collabSession.getCursors()
            );
            sendMessage(session, stateMessage);
            sendMessage(session, EditingLockStateMessage.of(
                    sessionId,
                    editingLockService.listLocks(sessionId, UserContext.of(oderId, username, Role.NORMAL_USER)).getLocks()
            ));

            log.info("WebSocket connected successfully: session[{}], user[{}]", sessionId, oderId);
        } catch (Exception e) {
            log.error("Error in afterConnectionEstablished: {}", e.getMessage(), e);
            try {
                session.close(CloseStatus.SERVER_ERROR.withReason("Internal error: " + e.getMessage()));
            } catch (Exception closeError) {
                log.error("Failed to close session after error", closeError);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, @NotNull TextMessage message) {
        String sessionId = (String) session.getAttributes().get(SESSION_ID_ATTR);
        String oderId = (String) session.getAttributes().get(USER_ID_ATTR);
        String username = (String) session.getAttributes().get(USERNAME_ATTR);
        UserContext userContext = UserContext.of(oderId, username, Role.NORMAL_USER);

        try {
            String payload = message.getPayload();

            if ("ping".equals(payload) || payload.startsWith("{\"type\":\"ping\"")) {
                sessionManager.updateHeartbeat(sessionId, oderId);
                sendMessage(session, Map.of("type", "pong"));
                return;
            }

            BaseMessage baseMsg = objectMapper.readValue(payload, BaseMessage.class);
            String type = baseMsg.getType();

            if ("operation".equals(type)) {
                OperationMessage opMsg = objectMapper.readValue(payload, OperationMessage.class);
                TextOperation operation = opMsg.toTextOperation();

                CollaborationSession collabSession = domainService.getSession(sessionId);
                domainService.validateUserInSession(collabSession, oderId);
                domainService.validateSessionNotExpired(collabSession);
                editingLockService.validateOperationAllowed(collabSession.getId(), operation, oderId);

                var serverOps = collabSession.getOperationsSince(operation.getClientVersion());
                TextOperation transformedOp = domainService.transformOperation(operation, serverOps);
                domainService.addOperation(collabSession, transformedOp, userContext);
                editingLockService.rebaseLocks(collabSession.getId(), collabSession.getDocumentId(), transformedOp, oderId, userContext);
                sessionRepository.save(collabSession);

                sessionManager.broadcast(
                        sessionId,
                        OperationMessage.fromTextOperation(
                                sessionId,
                                transformedOp,
                                collabSession.getVersion().getVersion()
                        ),
                        oderId
                );
                sessionManager.broadcast(
                        sessionId,
                        EditingLockStateMessage.of(
                                sessionId,
                                editingLockService.listLocks(sessionId, userContext).getLocks()
                        ),
                        oderId
                );
                sendMessage(session, OperationAckMessage.success(sessionId, collabSession.getVersion().getVersion()));
                return;
            }

            if ("operation_batch".equals(type)) {
                OperationMessage opMsg = objectMapper.readValue(payload, OperationMessage.class);

                CollaborationSession collabSession = domainService.getSession(sessionId);
                domainService.validateUserInSession(collabSession, oderId);
                domainService.validateSessionNotExpired(collabSession);

                if (opMsg.getOperations() != null) {
                    for (OperationMessage.OperationData opData : opMsg.getOperations()) {
                        TextOperation operation = convertOperationData(opData, oderId);
                        editingLockService.validateOperationAllowed(collabSession.getId(), operation, oderId);
                        var serverOps = collabSession.getOperationsSince(operation.getClientVersion());
                        TextOperation transformedOp = domainService.transformOperation(operation, serverOps);
                        domainService.addOperation(collabSession, transformedOp, userContext);
                        editingLockService.rebaseLocks(collabSession.getId(), collabSession.getDocumentId(), transformedOp, oderId, userContext);
                        sessionManager.broadcast(
                                sessionId,
                                OperationMessage.fromTextOperation(
                                        sessionId,
                                        transformedOp,
                                        collabSession.getVersion().getVersion()
                                ),
                                oderId
                        );
                    }
                    sessionRepository.save(collabSession);
                    sessionManager.broadcast(
                            sessionId,
                            EditingLockStateMessage.of(
                                    sessionId,
                                    editingLockService.listLocks(sessionId, userContext).getLocks()
                            ),
                            oderId
                    );
                    sendMessage(session, OperationAckMessage.success(sessionId, collabSession.getVersion().getVersion()));
                }
                return;
            }

            if ("cursor".equals(type)) {
                CursorMessage cursorMsg = objectMapper.readValue(payload, CursorMessage.class);
                CursorPosition cursor = CursorPosition.of(
                        oderId,
                        username,
                        cursorMsg.getPosition() != null ? cursorMsg.getPosition() : 0,
                        cursorMsg.getSelectionStart() != null ? cursorMsg.getSelectionStart() : 0,
                        cursorMsg.getSelectionEnd() != null ? cursorMsg.getSelectionEnd() : 0
                );

                CollaborationSession collabSession = domainService.getSession(sessionId);
                domainService.validateUserInSession(collabSession, oderId);
                domainService.updateCursor(collabSession, oderId, cursor, userContext);
                sessionRepository.save(collabSession);

                CursorMessage broadcastMsg = CursorMessage.of(
                        sessionId, oderId, username,
                        cursorMsg.getPosition() != null ? cursorMsg.getPosition() : 0,
                        cursorMsg.getSelectionStart() != null ? cursorMsg.getSelectionStart() : 0,
                        cursorMsg.getSelectionEnd() != null ? cursorMsg.getSelectionEnd() : 0
                );
                sessionManager.broadcast(sessionId, broadcastMsg, oderId);
                return;
            }

        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
            sendMessage(session, OperationAckMessage.failure(sessionId, 0, e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        String sessionId = (String) session.getAttributes().get(SESSION_ID_ATTR);
        String oderId = (String) session.getAttributes().get(USER_ID_ATTR);
        String username = (String) session.getAttributes().get(USERNAME_ATTR);

        if (sessionId != null && oderId != null) {
            sessionManager.removeSession(sessionId, session);

            try {
                CollaborationSession collabSession = domainService.getSessionOptional(sessionId).orElse(null);
                if (collabSession != null) {
                    UserContext userContext = UserContext.of(oderId, username, Role.NORMAL_USER);
                    domainService.leaveSession(collabSession, userContext);
                    sessionRepository.save(collabSession);
                    editingLockService.releaseUserLocks(sessionId, oderId, userContext);
                    sessionManager.broadcast(
                            sessionId,
                            SessionStateMessage.of(
                                    sessionId,
                                    collabSession.getVersion().getVersion(),
                                    collabSession.getActiveUsers().stream().toList(),
                                    collabSession.getCursors()
                            ),
                            oderId
                    );
                    sessionManager.broadcast(
                            sessionId,
                            EditingLockStateMessage.of(
                                    sessionId,
                                    editingLockService.listLocks(sessionId, userContext).getLocks()
                            ),
                            oderId
                    );
                }
            } catch (Exception e) {
                log.warn("Error leaving session: {}", e.getMessage());
            }

            log.info("WebSocket closed: session[{}], user[{}], status[{}]", sessionId, oderId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, @NotNull Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", exception.getMessage(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    private String extractSessionId(WebSocketSession session) {
        String uri = session.getUri() != null ? session.getUri().toString() : "";
        int idx = uri.lastIndexOf("/ws/collaboration/");
        if (idx > 0) {
            String sessionId = uri.substring(idx + "/ws/collaboration/".length());
            int queryIdx = sessionId.indexOf('?');
            if (queryIdx > 0) {
                sessionId = sessionId.substring(0, queryIdx);
            }
            return sessionId;
        }
        return null;
    }

    private String extractUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        for (String param : query.split("&")) {
            if (param.startsWith("userId=")) {
                return param.substring("userId=".length());
            }
        }
        return null;
    }

    private String extractUsername(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        for (String param : query.split("&")) {
            if (param.startsWith("username=")) {
                return param.substring("username=".length());
            }
        }
        return "Unknown";
    }

    private void sendMessage(WebSocketSession session, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }

    private TextOperation convertOperationData(OperationMessage.OperationData opData, String oderId) {
        TextOperationType opType = opData.getOperationType();
        Integer pos = opData.getPosition();
        String contentVal = opData.getContent();
        Integer len = opData.getLength();
        String userId = opData.getUserId() != null ? opData.getUserId() : oderId;
        Long version = opData.getClientVersion();

        if (opType == null) {
            throw new IllegalArgumentException("operationType is required");
        }
        if (pos == null) {
            throw new IllegalArgumentException("position is required");
        }

        long finalVersion = version != null ? version : 0L;

        if (opType == TextOperationType.INSERT) {
            return TextOperation.insert(userId, pos, contentVal, finalVersion);
        } else if (opType == TextOperationType.DELETE) {
            return TextOperation.delete(userId, pos, len != null ? len : 0, finalVersion);
        } else {
            return TextOperation.retain(userId, pos, len != null ? len : 0, finalVersion);
        }
    }

    @Setter
    @Getter
    public static class BaseMessage {
        private String type;
    }
}
