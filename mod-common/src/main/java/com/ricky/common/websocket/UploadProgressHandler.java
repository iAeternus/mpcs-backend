package com.ricky.common.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadProgressHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> uploadSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String uploadId = extractUploadId(session);
        if (uploadId != null) {
            uploadSessions.computeIfAbsent(uploadId, k -> ConcurrentHashMap.newKeySet()).add(session);
            log.info("WebSocket connected for upload: {}", uploadId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String uploadId = extractUploadId(session);
        if (uploadId != null) {
            Set<WebSocketSession> sessions = uploadSessions.get(uploadId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    uploadSessions.remove(uploadId);
                }
            }
            log.info("WebSocket disconnected for upload: {}", uploadId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    public void sendProgress(String uploadId, int uploadedChunks, int totalChunks) {
        Set<WebSocketSession> sessions = uploadSessions.get(uploadId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            UploadProgress progress = new UploadProgress(uploadId, uploadedChunks, totalChunks);
            String json = objectMapper.writeValueAsString(progress);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("Failed to send message to session: {}", session.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send progress", e);
        }
    }

    private String extractUploadId(WebSocketSession session) {
        String uri = session.getUri() != null ? session.getUri().toString() : "";
        int idx = uri.lastIndexOf("/");
        return idx > 0 ? uri.substring(idx + 1) : null;
    }

    public record UploadProgress(String uploadId, int uploadedChunks, int totalChunks) {
        public int getPercent() {
            return totalChunks > 0 ? (uploadedChunks * 100) / totalChunks : 0;
        }
    }
}
