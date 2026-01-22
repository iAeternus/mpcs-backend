package com.ricky.common.domain;

import com.ricky.common.domain.marker.Identified;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.DomainEvent;
import com.ricky.common.event.LocalDomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static java.time.Instant.now;
import static lombok.AccessLevel.PROTECTED;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className AggregateRoot
 * @desc 聚合根<br>
 * 规范：<br>
 * 1. 实现类必须将无参构造私有<br>
 * 2. 实现类不允许有默认的setter<br>
 */
@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class AggregateRoot implements Identified {

    /**
     * 最多保留的操作日志数量
     */
    public static final int MAX_OPS_LOG_SIZE = 128;

    /**
     * 标识符，通过Snowflake算法生成
     */
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 创建时间
     */
    private Instant createdAt;

    /**
     * 创建人的UID
     */
    private String createdBy;

    /**
     * 创建人姓名
     */
    private String creator;

    /**
     * 更新时间
     */
    private Instant updatedAt;

    /**
     * 更新人UID
     */
    private String updatedBy;

    /**
     * 更新人姓名
     */
    private String updater;

    /**
     * 领域事件列表，用于临时存放完成某个业务流程中所发出的事件，会被BaseRepository保存到事件表中<br>
     * 不会落库在聚合根表中
     */
    @Transient
    private List<DomainEvent> events;

    /**
     * 本地领域事件列表，提供轻量级的事务后处理方案
     */
    @Transient
    private List<LocalDomainEvent> localEvents;

    /**
     * 操作日志
     */
    private LinkedList<OpsLog> opsLogs;

    /**
     * 版本号，实现乐观锁
     */
    @Version
    @Getter(AccessLevel.PRIVATE)
    private Long _version;

    // TODO 以后考虑增加逻辑删除字段

    protected AggregateRoot(String id, UserContext userContext) {
        requireNotBlank(id, "ID must not be blank.");

        this.id = id;
        this.userId = userContext.getUid();
        this.createdAt = now();
        this.createdBy = userContext.getUid();
        this.creator = userContext.getUsername();
    }

    protected AggregateRoot(String id, String userId, UserContext userContext) {
        requireNotBlank(id, "AR ID must not be blank.");
        requireNotBlank(userId, "User ID must not be blank.");

        this.id = id;
        this.userId = userId;
        this.createdAt = now();
        this.createdBy = userContext.getUid();
        this.creator = userContext.getUsername();
    }

    /**
     * 添加操作日志
     *
     * @param note 记录
     */
    protected void addOpsLog(String note, UserContext userContext) {
        if (!userContext.isLoggedIn()) return;
        OpsLog log = OpsLog.builder()
                .note(note)
                .optAt(now())
                .optBy(userContext.getUid())
                .obn(userContext.getUsername())
                .build();
        List<OpsLog> opsLogs = allOpsLogs();
        opsLogs.add(log);
        // 最多保留最近MAX_OPS_LOG_SIZE条
        if (opsLogs.size() > MAX_OPS_LOG_SIZE) {
            this.opsLogs.remove();
        }

        this.updatedAt = now();
        this.updatedBy = userContext.getUid();
        this.updater = userContext.getUsername();
    }

    /**
     * 获取所有操作日志
     *
     * @return 操作日志集合，如果没有则返回空集合
     */
    private List<OpsLog> allOpsLogs() {
        if (isNull(opsLogs)) {
            this.opsLogs = new LinkedList<>();
        }
        return opsLogs;
    }

    /**
     * 触发事件
     *
     * @param event 事件
     */
    protected void raiseEvent(DomainEvent event) {
        event.setArInfo(this);
        allEvents().add(event);
    }

    /**
     * 获取所有领域事件
     *
     * @return 领域事件集合，没有就返回空集合
     */
    private List<DomainEvent> allEvents() {
        if (isNull(events)) {
            this.events = new ArrayList<>();
        }
        return events;
    }

    /**
     * 触发本地事件
     */
    protected void raiseLocalEvent(LocalDomainEvent event) {
        allLocalEvents().add(event);
    }

    /**
     * 拉取所有本地领域事件，会清空聚合根本地领域事件列表
     */
    public List<LocalDomainEvent> pullLocalEvents() {
        if (localEvents == null) {
            return List.of();
        }
        List<LocalDomainEvent> copy = List.copyOf(localEvents);
        localEvents.clear();
        return copy;
    }

    private List<LocalDomainEvent> allLocalEvents() {
        if (isNull(localEvents)) {
            this.localEvents = new ArrayList<>();
        }
        return localEvents;
    }
}
