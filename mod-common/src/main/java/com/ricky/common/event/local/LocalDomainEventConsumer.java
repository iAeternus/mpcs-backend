package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;
import com.ricky.common.event.local.track.LocalDomainEventTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalDomainEventConsumer {

    private final List<LocalDomainEventHandler<?>> handlers;
    private final LocalDomainEventTracker tracker;

    private final Map<String, Boolean> consumedMap = new ConcurrentHashMap<>();

    public void consume(LocalDomainEvent event) {
        long start = System.currentTimeMillis();
        String eventId = event.getEventId();

        log.info("Start consuming local event [{}]", eventId);
        consumedMap.put(eventId, false);

        tracker.record(event);
        handlers.forEach(handler -> {
            try {
                handler.handle(event);
            } catch (Exception e) {
                log.error("Handler [{}] failed to consume local event [{}]",
                        handler.getClass().getSimpleName(),
                        eventId, e);
                throw e;
            }
        });

        long cost = System.currentTimeMillis() - start;
        consumedMap.put(eventId, true);

        log.info("Local event [{}] fully consumed in {} ms", eventId, cost);
    }

    public boolean isEventFullyConsumed(String eventId) {
        Boolean done = consumedMap.get(eventId);
        if (Boolean.TRUE.equals(done)) {
            consumedMap.remove(eventId);
            return true;
        }
        return false;
    }
}
