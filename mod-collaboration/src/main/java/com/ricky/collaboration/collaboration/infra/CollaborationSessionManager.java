package com.ricky.collaboration.collaboration.infra;

import com.ricky.common.json.JsonCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollaborationSessionManager {

    private final JsonCodec jsonCodec;

    private final Map<String, Set<WebSocketSession>> sessionConnections = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Instant>> heartbeats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;
    private static final long SESSION_TIMEOUT_SECONDS = 120;

    public void addSession(String sessionId, WebSocketSession session) {
        sessionConnections.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        heartbeats.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                .put(extractUserId(session), Instant.now());
        log.debug("Added session connection: session[{}], connections[{}]",
                sessionId, sessionConnections.get(sessionId).size());
    }

    public void removeSession(String sessionId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionConnections.get(sessionId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionConnections.remove(sessionId);
            }
        }

        Map<String, Instant> hb = heartbeats.get(sessionId);
        if (hb != null) {
            hb.remove(extractUserId(session));
            if (hb.isEmpty()) {
                heartbeats.remove(sessionId);
            }
        }

        log.debug("Removed session connection: session[{}], remaining[{}]",
                sessionId, sessions != null ? sessions.size() : 0);
    }

    public void broadcast(String sessionId, Object message, String excludeUserId) {
        Set<WebSocketSession> sessions = sessionConnections.get(sessionId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String json;
        try {
            json = jsonCodec.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Failed to serialize message: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(json);

        for (WebSocketSession session : sessions) {
            String userId = extractUserId(session);
            if (userId != null && userId.equals(excludeUserId)) {
                continue;
            }

            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.warn("Failed to send message to session: {}", session.getId());
                }
            }
        }
    }

    public void updateHeartbeat(String sessionId, String oderId) {
        Map<String, Instant> hb = heartbeats.get(sessionId);
        if (hb != null) {
            hb.put(oderId, Instant.now());
        }
    }

    public Set<WebSocketSession> getSessions(String sessionId) {
        return sessionConnections.getOrDefault(sessionId, Set.of());
    }

    public int getConnectionCount(String sessionId) {
        Set<WebSocketSession> sessions = sessionConnections.get(sessionId);
        return sessions != null ? sessions.size() : 0;
    }

    private String extractUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    public void startHeartbeatChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            Instant timeout = Instant.now().minusSeconds(SESSION_TIMEOUT_SECONDS);

            heartbeats.forEach((sessionId, userHeartbeats) -> {
                userHeartbeats.entrySet().removeIf(entry -> {
                    if (entry.getValue().isBefore(timeout)) {
                        log.info("User[{}] connection timed out in session[{}]", entry.getKey(), sessionId);
                        return true;
                    }
                    return false;
                });
            });
        }, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
