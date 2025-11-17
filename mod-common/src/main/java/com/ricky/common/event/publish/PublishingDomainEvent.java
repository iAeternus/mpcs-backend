package com.ricky.common.event.publish;

import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.ricky.common.constants.ConfigConstant.PUBLISHING_DOMAIN_EVENT_COLLECTION;
import static com.ricky.common.event.publish.DomainEventPublishStatus.CREATED;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 领域事件包装类
 * @note 增加了“状态”和“发布次数”字段，以跟踪发布过程
 */
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(PUBLISHING_DOMAIN_EVENT_COLLECTION)
@TypeAlias("PUBLISHING_DOMAIN_EVENT")
public class PublishingDomainEvent {

    private String id;
    private DomainEvent event;
    private DomainEventPublishStatus status;
    private int publishedCount;
    private Instant raisedAt;

    public PublishingDomainEvent(DomainEvent event) {
        requireNonNull(event, "Domain event must not be null.");

        this.id = event.getId();
        this.event = event;
        this.status = CREATED;
        this.publishedCount = 0;
        this.raisedAt = event.getRaisedAt();
    }

}
