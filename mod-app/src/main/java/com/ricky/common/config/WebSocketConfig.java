package com.ricky.common.config;

import com.ricky.common.websocket.UploadProgressHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final UploadProgressHandler uploadProgressHandler;

    @PostConstruct
    public void init() {
        log.info("WebSocketConfig initialized");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(uploadProgressHandler, "/ws/upload/{uploadId}")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean servletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean factory = new ServletServerContainerFactoryBean();
        factory.setMaxTextMessageBufferSize(16 * 1024 * 1024); // 16MB
        factory.setMaxBinaryMessageBufferSize(16 * 1024 * 1024);
        factory.setMaxSessionIdleTimeout(60 * 60 * 1000L); // 1 hour
        log.info("Configured WebSocket message buffer size: 16MB");
        return factory;
    }
}
