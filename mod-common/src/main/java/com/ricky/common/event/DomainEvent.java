package com.ricky.common.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ricky.common.context.ThreadLocalContext;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.json.JsonTypeDefine;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

import static com.ricky.common.utils.ValidationUtil.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author Ricky
 * @version 1.0
 * @date 2024/9/5
 * @className DomainEvent
 * @desc 领域事件 <br>
 * 这里ar代指AggregateRoot
 * DomainEvent既要保证能支持MongoDB的序列化/反序列化，有要能够通过Jackson序列化/反序列化（因为要发送到Redis）<br>
 * @see JsonTypeDefine 其子类必须加@JsonTypeDefine注解才可正常序列化<br>
 */
@Getter
@FieldNameConstants
@NoArgsConstructor(access = PROTECTED)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
public abstract class DomainEvent {

    /**
     * 事件ID，不能为空
     */
    private String id;

    /**
     * 事件对应的用户ID，不能为空
     */
    private String arUserId;

    /**
     * 事件对应的聚合根ID，不能为空
     */
    private String arId;

    /**
     * 事件类型
     */
    private DomainEventType type;

    /**
     * 引发该事件的memberId
     */
    private String raisedBy;

    /**
     * 事件产生时间
     */
    private Instant raisedAt;

    protected DomainEvent(DomainEventType type) {
        requireNonNull(type, "Domain event type must not be null.");

        this.id = newEventId();
        this.type = type;
        this.raisedBy = ThreadLocalContext.getContext().getUid();
        this.raisedAt = Instant.now();
    }

    /**
     * 生成事件id
     *
     * @return 事件id
     */
    public String newEventId() {
        return "EVT" + SnowflakeIdGenerator.newSnowflakeId();
    }

    /**
     * 设置聚合根信息
     *
     * @param ar 聚合根
     */
    public void setArInfo(AggregateRoot ar) {
        this.arUserId = ar.getUserId();
        this.arId = ar.getId();
    }

}
