package com.ricky.apitest;

import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.DomainEventType;
import com.ricky.common.event.LocalDomainEvent;
import com.ricky.common.event.consume.DomainEventConsumer;
import com.ricky.common.event.local.LocalDomainEventConsumer;
import com.ricky.common.event.local.track.LocalDomainEventTracker;
import com.ricky.common.event.publish.PublishingDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked"})
public class EventUtils {

    private final MongoTemplate mongoTemplate;
    private final DomainEventConsumer<DomainEvent> domainEventConsumer;
    private final LocalDomainEventConsumer localDomainEventConsumer;
    private final LocalDomainEventTracker localTracker;

    public <T extends DomainEvent> T latestEventFor(String arId, DomainEventType type, Class<T> eventClass) {
        Query query = query(where(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.arId).is(arId)
                .and(PublishingDomainEvent.Fields.event + "." + DomainEvent.Fields.type).is(type))
                .with(by(DESC, PublishingDomainEvent.Fields.raisedAt));
        PublishingDomainEvent publishingDomainEvent = mongoTemplate.findOne(query, PublishingDomainEvent.class);
        if (publishingDomainEvent == null) {
            return null;
        }
        return (T) publishingDomainEvent.getEvent();
    }

    /**
     * 领域事件
     */

    // 默认10秒
    public <T extends DomainEvent> void awaitEventConsumed(T event) {
        awaitEventConsumed(event, 10, SECONDS);
    }

    public <T extends DomainEvent> void awaitEventConsumed(T event, long timeout, TimeUnit unit) {
        long start = System.currentTimeMillis();

        await().atMost(timeout, unit)
                .pollInterval(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                .until(() -> domainEventConsumer.isEventFullyConsumed(event));

        long cost = System.currentTimeMillis() - start;
        log.info("Event [{}] fully consumed after {} ms", event.getId(), cost);
    }

    public <T extends DomainEvent> void awaitLatestEventConsumed(String arId, DomainEventType type, Class<T> eventClass) {
        T evt = latestEventFor(arId, type, eventClass);
        awaitEventConsumed(evt);
    }

    /**
     * 本地领域事件
     */

    public <T extends LocalDomainEvent> void awaitLatestLocalEventConsumed(String arId, Class<T> eventClass) {
        awaitLatestLocalEventConsumed(arId, eventClass, 10, TimeUnit.SECONDS);
    }

    public <T extends LocalDomainEvent> void awaitLatestLocalEventConsumed(
            String arId,
            Class<T> eventClass,
            long timeout,
            TimeUnit unit) {
        long start = System.currentTimeMillis();

        await().atMost(timeout, unit)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    String eventId = localTracker.latestEventId(arId, eventClass);
                    return eventId != null
                            && localDomainEventConsumer.isEventFullyConsumed(eventId);
                });

        log.info("LocalEvent [{}] for aggregate [{}] fully consumed in {} ms",
                eventClass.getSimpleName(),
                arId,
                System.currentTimeMillis() - start);
    }

}
