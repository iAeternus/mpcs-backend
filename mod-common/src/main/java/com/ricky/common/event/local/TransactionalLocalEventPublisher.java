package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 事务后事件发布器
 * 在Spring事务提交后发布事件，用于处理需要在事务成功后执行的操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalLocalEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    // 线程安全的事件队列，用于批量处理
    private final ThreadLocal<ConcurrentLinkedQueue<LocalDomainEvent>> eventQueue =
            ThreadLocal.withInitial(ConcurrentLinkedQueue::new);

    /**
     * 发布事务后事件
     * 如果当前在事务中，事件将在事务提交后发布
     * 如果不在事务中，事件立即发布
     */
    public void publishAfterCommit(LocalDomainEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 在事务中，注册事务同步
            registerTransactionSynchronization(event);
        } else {
            // 不在事务中，立即发布
            publishEvent(event);
        }
    }

    /**
     * 批量发布事务后事件
     */
    public void publishAfterCommit(Collection<LocalDomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 在事务中，添加到队列并注册同步
            eventQueue.get().addAll(events);
            registerBatchTransactionSynchronization();
        } else {
            // 不在事务中，立即发布所有事件
            events.forEach(this::publishEvent);
        }
    }

    private void registerTransactionSynchronization(LocalDomainEvent event) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publishEvent(event);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.debug("Transaction rolled back, event not published: {}", event.getClass().getSimpleName());
                }
            }
        });
    }

    private void registerBatchTransactionSynchronization() {
        if (TransactionSynchronizationManager.getSynchronizations().stream()
                .noneMatch(sync -> sync instanceof BatchEventSynchronization)) {

            TransactionSynchronizationManager.registerSynchronization(new BatchEventSynchronization());
        }
    }

    private void publishEvent(LocalDomainEvent event) {
        try {
            log.debug("Publishing local domain event: {} for aggregate: {}",
                    event.getClass().getSimpleName(), event.getAggregateId());
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish local domain event: {}", event, e);
            // 可以将失败的事件发送到死信队列或记录到数据库
        }
    }

    /**
     * 批量事件同步处理器
     */
    private class BatchEventSynchronization implements TransactionSynchronization {
        @Override
        public void afterCommit() {
            ConcurrentLinkedQueue<LocalDomainEvent> queue = eventQueue.get();
            List<LocalDomainEvent> events = new ArrayList<>();

            // 取出所有事件
            while (!queue.isEmpty()) {
                LocalDomainEvent event = queue.poll();
                if (event != null) {
                    events.add(event);
                }
            }

            // 批量发布
            if (!events.isEmpty()) {
                log.debug("Publishing {} local domain events after transaction commit", events.size());
                events.forEach(TransactionalLocalEventPublisher.this::publishEvent);
            }
        }

        @Override
        public void afterCompletion(int status) {
            // 清理ThreadLocal，防止内存泄漏
            eventQueue.remove();

            if (status == STATUS_ROLLED_BACK) {
                log.debug("Transaction rolled back, batch events not published");
            }
        }
    }
}