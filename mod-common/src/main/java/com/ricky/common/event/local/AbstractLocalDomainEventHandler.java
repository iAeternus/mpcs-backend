package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLocalDomainEventHandler<E extends LocalDomainEvent>
        implements LocalDomainEventHandler<E> {

    private final Class<E> eventType;

    @SuppressWarnings("unchecked")
    protected AbstractLocalDomainEventHandler() {
        this.eventType = (Class<E>)
                ((java.lang.reflect.ParameterizedType)
                        getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
    }

    @Override
    public final void handle(LocalDomainEvent event) {
        if (!eventType.isInstance(event)) {
            return;
        }

        E typedEvent = eventType.cast(event);

        try {
            doHandle(typedEvent);
        } catch (Exception e) {
            log.error("Failed to handle local domain event for Aggregate[{}]",
                    typedEvent.getAggregateId(), e);
            recordFailedHandle(typedEvent);
            throw e;
        }
    }

    /**
     * 实际处理事件
     */
    protected abstract void doHandle(E event);

    protected void recordFailedHandle(E event) {
        log.warn("Recording failed local event for Aggregate[{}]", event.getAggregateId());
    }
}
