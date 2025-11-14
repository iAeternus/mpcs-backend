package com.ricky.common.domain.event.consume;

import com.ricky.common.domain.event.DomainEvent;
import com.ricky.common.domain.event.DomainEventConsumer;
import com.ricky.common.tracing.TracingService;
import com.ricky.common.utils.MyObjectMapper;
import io.micrometer.tracing.ScopedSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className RedisDomainEventListener
 * @desc
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDomainEventListener implements DomainEventListener, StreamListener<String, ObjectRecord<String, String>> {

    private final MyObjectMapper objectMapper;
    private final DomainEventConsumer domainEventConsumer;
    private final TracingService tracingService;

    @Override
    public void onMessage(ObjectRecord<String, String> message) {
        ScopedSpan scopedSpan = tracingService.startNewSpan("domain-event-listener");

        String jsonString = message.getValue();
        DomainEvent domainEvent = objectMapper.readValue(jsonString, DomainEvent.class);
        try {
            domainEventConsumer.consume(domainEvent);
        } catch (Throwable t) {
            log.error("Failed to listen domain event[{}:{}].", domainEvent.getType(), domainEvent.getId(), t);
        }

        scopedSpan.end();
    }
}
