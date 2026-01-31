package com.ricky.common.event;

public interface EventConsumedTracker {

    /**
     * 是否已完全消费
     */
    boolean isConsumed(String eventId);

    /**
     * 获取某聚合某类型事件的最新 eventId
     */
    String latestEventId(String aggregateId, Class<?> eventType);

}
