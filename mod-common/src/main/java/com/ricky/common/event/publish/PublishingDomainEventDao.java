package com.ricky.common.event.publish;

import com.ricky.common.event.DomainEvent;

import java.util.List;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/10
 * @className DomainEventDao
 * @desc 领域事件数据访问对象
 */
public interface PublishingDomainEventDao {

    /**
     * @param events 领域事件
     * @brief 新增领域事件
     */
    void stage(List<DomainEvent> events);

    /**
     * @param startId 起始ID
     * @param limit   个数
     * @return 领域事件集合
     * @brief 查询ID从startId开始的limit个领域事件
     */
    List<DomainEvent> stagedEvents(String startId, int limit);

    /**
     * 根据事件ID列表批量查询领域事件
     *
     * @param ids 事件ID列表
     * @return 领域事件列表，没找到返回空列表
     */
    List<DomainEvent> byIds(List<String> ids);

    /**
     * 成功发布事件后的回调函数
     *
     * @param eventId 领域事件ID
     */
    void successPublish(String eventId);


    /**
     * 失败发布事件后的回调函数
     *
     * @param eventId 领域事件ID
     */
    void failPublish(String eventId);
}
