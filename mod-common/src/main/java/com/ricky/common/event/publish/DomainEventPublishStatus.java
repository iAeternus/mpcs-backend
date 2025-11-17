package com.ricky.common.event.publish;

/**
 * @brief 领域事件发布状态
 */
public enum DomainEventPublishStatus {

    /**
     * @brief 已创建
     */
    CREATED,

    /**
     * @brief 发布成功
     */
    PUBLISH_SUCCEED,

    /**
     * @brief 发布失败
     */
    PUBLISH_FAILED,
}
