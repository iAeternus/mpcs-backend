package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
public abstract class AbstractLocalDomainEventHandler<E extends LocalDomainEvent> implements LocalDomainEventHandler<E> {

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(E event) {
        if (!supports(event)) {
            return;
        }

        try {
            doHandle(event);
        } catch (Exception e) {
            log.error("Failed to handle local domain event for Aggregate[{}]", event.getAggregateId(), e);
            // 记录失败，通过补偿机制处理
            recordFailedHandle(event);
        }
    }

    protected abstract void doHandle(E event);

    /**
     * 记录失败的更新，用于后续补偿
     */
    protected void recordFailedHandle(E event) {
        // TODO: 可以将失败的更新记录到数据库或发送到死信队列，或通过定时任务或手动触发进行补偿
        log.warn("Recording failed local event for Aggregate[{}]", event.getAggregateId());
    }
}
