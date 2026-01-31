package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;

public interface ConsumingDomainEventDao<T extends DomainEvent> {

    /**
     * 判断事件是否已被消费
     *
     * @param consumingDomainEvent 领域事件ORM对象
     * @param handlerName          事件处理器类名
     * @return true=此事件此前从未被使用过
     */
    boolean recordAsConsumed(ConsumingDomainEvent<T> consumingDomainEvent, String handlerName);

    /**
     * 查询可以处理该事件的事件处理器数量
     */
    long countByEventId(String eventId);

}
