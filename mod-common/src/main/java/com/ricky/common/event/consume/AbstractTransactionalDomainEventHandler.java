package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * 将事件处理和事件记录置于同一事务中，以确保这两者具有原子性（即要么全部成功，要么全部失败）
 * 通常在事件处理过程中存在数据库变更的情况下会使用此方法
 * 最佳实践是尽可能采用 AbstractTransactionalDomainEventHandler 和idempotent特性
 */
public abstract class AbstractTransactionalDomainEventHandler<T extends DomainEvent> extends AbstractDomainEventHandler<T> {

    @Override
    @Transactional
    public void handle(ConsumingDomainEvent<T> consumingDomainEvent) {
        super.handle(consumingDomainEvent);
    }

}
