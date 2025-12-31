package com.ricky.common.event;

import com.ricky.common.domain.AggregateRootType;
import lombok.Getter;

import static com.ricky.common.domain.AggregateRootType.*;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/5
 * @className DomainEventTypeEnum
 * @desc 领域事件类型枚举
 */
@Getter
public enum DomainEventType {

    FILE_UPLOADED(FILE),
    FILE_DELETED(FILE),
    USER_CREATED(USER),
    FOLDER_HIERARCHY_CHANGED(FOLDER),
    FOLDER_CREATED(FOLDER),
    FOLDER_RENAMED(FOLDER),
    FOLDER_DELETED(FOLDER),
    // add here...
    ;

    private final AggregateRootType arType;

    DomainEventType(AggregateRootType arType) {
        this.arType = arType;
    }

}
