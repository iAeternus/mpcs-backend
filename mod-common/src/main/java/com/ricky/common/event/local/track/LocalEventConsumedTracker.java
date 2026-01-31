package com.ricky.common.event.local.track;

import com.ricky.common.event.EventConsumedTracker;
import com.ricky.common.event.local.LocalDomainEventConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalEventConsumedTracker implements EventConsumedTracker {

    private final LocalDomainEventConsumer consumer;
    private final LocalDomainEventTracker tracker;

    @Override
    public boolean isConsumed(String eventId) {
        return consumer.isEventFullyConsumed(eventId);
    }

    @Override
    public String latestEventId(String arId, Class<?> type) {
        return tracker.latestEventId(arId, type);
    }
}
