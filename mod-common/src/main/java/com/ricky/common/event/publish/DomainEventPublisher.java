package com.ricky.common.event.publish;

import com.ricky.common.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ricky.common.utils.ValidationUtil.isEmpty;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.Instant.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private static final String MIN_START_EVENT_ID = "EVT00000000000000001";
    private static final int BATCH_SIZE = 100;
    private static final int MAX_FETCH_SIZE = 10000;

    private final LockingTaskExecutor lockingTaskExecutor;
    private final PublishingDomainEventDao publishingDomainEventDao;
    private final DomainEventSender domainEventSender;
    private final TaskExecutor taskExecutor;

    public int publishStagedDomainEvents() {
        try {
            // 使用分布式锁来确保同一时间只有一个节点运行，否则可能会导致事件重复出现的情况
            var result = lockingTaskExecutor.executeWithLock(this::doPublishStagedDomainEvents,
                    new LockConfiguration(now(), "publish-domain-events", ofMinutes(1), ofMillis(1)));
            Integer publishedCount = result.getResult();
            int count = publishedCount != null ? publishedCount : 0;
            log.debug("Published {} domain events.", count);
            return count;
        } catch (Throwable e) {
            log.error("Error happened while publish domain events.", e);
            return 0;
        }
    }

    private int doPublishStagedDomainEvents() {
        int counter = 0;
        String startEventId = MIN_START_EVENT_ID;
        List<CompletableFuture<String>> futures = new ArrayList<>();

        while (true) {
            // 分页查询事件
            List<DomainEvent> domainEvents = publishingDomainEventDao.stagedEvents(startEventId, BATCH_SIZE);
            if (isEmpty(domainEvents)) {
                break;
            }

            // 异步发送所有事件
            for (DomainEvent event : domainEvents) {
                var future = this.domainEventSender.send(event)
                        .whenCompleteAsync((eventId, ex) -> {
                            if (ex == null) {
                                this.publishingDomainEventDao.successPublish(eventId);
                            } else {
                                this.publishingDomainEventDao.failPublish(eventId);
                                log.error("Error publishing domain event [{}]:", eventId, ex);
                            }
                        }, taskExecutor);
                futures.add(future);
            }

            counter = domainEvents.size() + counter;
            if (counter >= MAX_FETCH_SIZE) {
                break;
            }

            startEventId = domainEvents.get(domainEvents.size() - 1).getId(); // Start event ID for next batch
        }

        // 等待所有异步操作完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return counter;
    }

}
