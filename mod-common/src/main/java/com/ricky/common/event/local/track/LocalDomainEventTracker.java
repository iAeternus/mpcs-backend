package com.ricky.common.event.local.track;

import com.ricky.common.event.LocalDomainEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalDomainEventTracker {

    private final Map<String, String> lastEventMap = new ConcurrentHashMap<>();

    public void record(LocalDomainEvent event) {
        lastEventMap.put(key(event.getAggregateId(), event.getClass()), event.getEventId());
    }

    public String latestEventId(String arId, Class<?> type) {
        return lastEventMap.get(key(arId, type));
    }

    private String key(String arId, Class<?> type) {
        return arId + "::" + type.getName();
    }
}
