package com.ricky.common.event;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

/**
 * 本地领域事件基类
 * 用于事务后的本地处理，不经过持久化
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class LocalDomainEvent {

    private String eventId;
    private String aggregateId;
    private String userId;
    private Instant occurredAt;

    protected <AR extends AggregateRoot> LocalDomainEvent(AR ar, UserContext userContext) {
        this.eventId = newLocalEventId();
        this.aggregateId = ar.getId();
        this.userId = userContext.getUid();
        this.occurredAt = Instant.now();
    }

    public static String newLocalEventId() {
        return "LOCAL_EVT" + SnowflakeIdGenerator.newSnowflakeId();
    }
}