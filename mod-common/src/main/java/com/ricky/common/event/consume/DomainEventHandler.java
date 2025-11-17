package com.ricky.common.event.consume;


import com.ricky.common.event.DomainEvent;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className DomainEventHandler
 * @desc 领域事件处理器
 */
public interface DomainEventHandler<T extends DomainEvent> {

    /**
     * @return true=是 false=否
     * @brief 事件处理是否具有幂等性
     * @note 默认情况下，所有处理程序都被假定为非幂等的
     */
    default boolean isIdempotent() {
        return false;
    }

    /**
     * @return 数值越小，优先级越高
     * @brief 事件处理优先级
     */
    default int priority() {
        return 0;
    }

    void handle(ConsumingDomainEvent<T> consumingDomainEvent);

}
