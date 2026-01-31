package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;

public interface LocalDomainEventHandler<E extends LocalDomainEvent> {

    /**
     * 框架调用
     */
    void handle(LocalDomainEvent event);

}
