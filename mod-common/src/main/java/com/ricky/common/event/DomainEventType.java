package com.ricky.common.event;

import com.ricky.common.domain.AggregateRootType;
import lombok.Getter;

import static com.ricky.common.domain.AggregateRootType.FILE;
import static com.ricky.common.domain.AggregateRootType.USER;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/5
 * @className DomainEventTypeEnum
 * @desc 领域事件类型枚举
 */
@Getter
public enum DomainEventType {

    FILE_UPLOADED_EVENT(FILE),
    USER_CREATED(USER)
    // add here...
    ;

    private final AggregateRootType arType;

    DomainEventType(AggregateRootType arType) {
        this.arType = arType;
    }

}
