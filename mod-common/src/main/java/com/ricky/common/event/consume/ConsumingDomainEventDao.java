package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;

public interface ConsumingDomainEventDao<T extends DomainEvent> {

    /**
     * @param consumingDomainEvent 领域事件包装器
     * @param handlerName          事件处理器类名
     * @return true=此事件此前从未被使用过
     * @brief 判断事件是否已被消费
     */
    boolean recordAsConsumed(ConsumingDomainEvent<T> consumingDomainEvent, String handlerName);

}
