package com.ricky.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 领域事件派生类型注册器
 */
public interface DomainEventSubtypeRegistrar {

    /**
     * 由于mod-common无法扫描bean，需要业务模块自行配置领域事件派生类json序列化
     */
    void register(ObjectMapper objectMapper);
}