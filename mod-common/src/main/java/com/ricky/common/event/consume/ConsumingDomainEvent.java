package com.ricky.common.event.consume;

import com.ricky.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import static com.ricky.common.constants.ConfigConstants.CONSUMING_DOMAIN_EVENT_COLLECTION;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * @brief 消费领域事件包装类
 * @note 如有需要，可以添加更多相关信息（例如事件是否被重新发送等），但不应与特定的消息传递中间件绑定在一起
 */
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PRIVATE)
@Document(CONSUMING_DOMAIN_EVENT_COLLECTION)
@TypeAlias("CONSUMING_DOMAIN_EVENT")
public class ConsumingDomainEvent<T extends DomainEvent> {

    private String eventId; // 领域事件ID
    private String type; // 领域事件类型
    private String handlerName; // 事件处理器名称
    private Instant consumedAt; // 消费时间
    private T event; // 领域事件

    public ConsumingDomainEvent(String eventId, String eventType, T event) {
        requireNotBlank(eventId, "Event ID must not be blank.");
        requireNotBlank(eventType, "Event type must not be blank.");
        requireNonNull(event, "Event must not be null.");

        this.eventId = eventId;
        this.type = eventType;
        this.consumedAt = Instant.now();
        this.event = event;
    }

}
