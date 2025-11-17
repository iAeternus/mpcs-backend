package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 此基础事件处理程序在处理事件之前会进行如下检查：
 * 1. 如果该处理程序是幂等的，则直接处理该事件并返回
 * 2. 如果该事件尚未被此处理程序处理，那么就对其进行处理并返回
 * 3. 如果该事件已被处理，则无需再进行任何操作；
 * 建议尽量采用 AbstractTransactionalDomainEventHandler，并保持尽可能的幂等性
 */
@Slf4j
public abstract class AbstractDomainEventHandler<T extends DomainEvent> implements DomainEventHandler<T> {

    @Autowired
    private ConsumingDomainEventDao<T> consumingDomainEventDao;

    @Override
    public void handle(ConsumingDomainEvent<T> consumingDomainEvent) {
        if (isIdempotent() || consumingDomainEventDao.recordAsConsumed(consumingDomainEvent, this.getClass().getSimpleName())) {
            doHandle(consumingDomainEvent.getEvent());
        } else {
            log.warn("Domain event[{}] has already been consumed by handler[{}], skip handling.",
                    consumingDomainEvent.getEventId(), this.getClass().getName());
        }
    }

    protected abstract void doHandle(T event);

}
