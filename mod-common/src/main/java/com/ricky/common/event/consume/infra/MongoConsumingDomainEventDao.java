package com.ricky.common.event.consume.infra;

import com.mongodb.client.result.UpdateResult;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.consume.ConsumingDomainEvent;
import com.ricky.common.event.consume.ConsumingDomainEventDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static com.ricky.common.event.consume.ConsumingDomainEvent.Fields.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoConsumingDomainEventDao<T extends DomainEvent> implements ConsumingDomainEventDao<T> {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean recordAsConsumed(ConsumingDomainEvent<T> consumingDomainEvent, String handlerName) {
        Query query = query(where(eventId)
                .is(consumingDomainEvent.getEventId())
                .and(handlerName)
                .is(handlerName));

        Update update = new Update()
                .setOnInsert(type, consumingDomainEvent.getType())
                .setOnInsert(consumedAt, consumingDomainEvent.getConsumedAt());

        UpdateResult result = this.mongoTemplate.update(ConsumingDomainEvent.class)
                .matching(query)
                .apply(update)
                .upsert();

        return result.getMatchedCount() == 0;
    }
}
