package com.ricky.collaboration.common.config;

import com.ricky.collaboration.collaboration.infra.CollaborationWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class CollaborationWebSocketConfig implements WebSocketConfigurer {

    private final CollaborationWebSocketHandler webSocketHandler;

    @PostConstruct
    public void init() {
        log.info("CollaborationWebSocketConfig initialized");
        log.info("webSocketHandler class: {}", webSocketHandler.getClass().getName());
    }

    @Bean
    public ServletServerContainerFactoryBean collaborationWebSocketContainer() {
        ServletServerContainerFactoryBean factory = new ServletServerContainerFactoryBean();
        factory.setMaxTextMessageBufferSize(16 * 1024 * 1024); // 16MB
        factory.setMaxBinaryMessageBufferSize(16 * 1024 * 1024);
        factory.setMaxSessionIdleTimeout(60 * 60 * 1000L);
        log.info("Configured collaboration WebSocket message buffer size: 16MB");
        return factory;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info(">>> registerWebSocketHandlers called");
        log.info(">>> Registry info: {}", registry);

        registry.addHandler(webSocketHandler, "/ws/collaboration/**")
                .addInterceptors(new CollaborationHandshakeInterceptor())
                .setAllowedOrigins("*");

        log.info(">>> WebSocket handler registered at /ws/collaboration/**");
    }

    @Slf4j
    static class CollaborationHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(
                org.springframework.http.server.ServerHttpRequest request,
                org.springframework.http.server.ServerHttpResponse response,
                org.springframework.web.socket.WebSocketHandler wsHandler,
                Map<String, Object> attributes) {
            log.info(">>> WS BEFORE HANDSHAKE: method={}, uri={}", request.getMethod(), request.getURI());
            log.info(">>> WS Headers - Sec-WebSocket-Key: {}, Sec-WebSocket-Version: {}, Origin: {}",
                    request.getHeaders().getFirst("Sec-WebSocket-Key"),
                    request.getHeaders().getFirst("Sec-WebSocket-Version"),
                    request.getHeaders().getFirst("Origin"));
            attributes.put("remoteAddress", request.getRemoteAddress());
            return true;
        }

        @Override
        public void afterHandshake(
                org.springframework.http.server.ServerHttpRequest request,
                org.springframework.http.server.ServerHttpResponse response,
                org.springframework.web.socket.WebSocketHandler wsHandler,
                Exception exception) {
            log.info(">>> WS AFTER HANDSHAKE: method={}, uri={}", request.getMethod(), request.getURI());
            if (exception != null) {
                log.error(">>> WS HANDSHAKE ERROR: {}", exception.getMessage(), exception);
            }
        }
    }
}
