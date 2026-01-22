package com.ricky.common.event.local;

import com.ricky.common.event.LocalDomainEvent;

public interface LocalDomainEventHandler<E extends LocalDomainEvent> {

    /**
     * 是否支持该事件
     */
    boolean supports(LocalDomainEvent event);

    /**
     * 处理事件（由框架调用）
     */
    void handle(E event);

}
