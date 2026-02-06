package com.ricky.group.domain;

import com.ricky.common.auth.Permission;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.group.domain.event.GroupDeletedEvent;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.domain.SpaceType.teamCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.MAX_GROUP_MANAGER_REACHED;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.notEquals;

/**
 * 权限组，描述用户集合对资源集合的权限集合，用来映射真实团队组织架构
 */
@Getter
@TypeAlias("group")
@Document(GROUP_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Group extends AggregateRoot {

    private String name; // 名称
    private boolean active; // 是否启用
    private Set<String> managers; // 管理员，members子集
    private Set<String> members; // 所有成员
    private String customId; // 文件夹层次结构自定义ID
    private Map<String, Set<Permission>> grants; // 资源ID -> 权限集合
    private InheritancePolicy inheritancePolicy; // 继承策略


    public Group(String name, UserContext userContext) {
        super(newGroupId(), userContext);
        init(name, userContext);
    }

    private void init(String name, UserContext userContext) {
        this.name = name;
        this.active = true;
        this.members = new HashSet<>();
        this.managers = new HashSet<>();
        this.customId = teamCustomId(getId());
        this.grants = new HashMap<>();
        this.inheritancePolicy = InheritancePolicy.NONE;
        addOpsLog("新建", userContext);
    }

    public static String newGroupId() {
        return GROUP_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void rename(String newName, UserContext userContext) {
        if (name.equals(newName)) {
            return;
        }

        this.name = newName;
        addOpsLog("重命名为：" + name, userContext);
    }

    public void addMembers(List<String> userIds, UserContext userContext) {
        Set<String> resultMembers = Stream.concat(members.stream(), userIds.stream()).collect(toImmutableSet());
        if (notEquals(members, resultMembers)) {
            raiseEvent(new GroupMembersChangedEvent(getId(), userIds, userContext));
        }

        this.members = resultMembers;
        addOpsLog("设置成员", userContext);
    }

    public void removeMember(String userId, UserContext userContext) {
        this.managers.remove(userId);

        Set<String> remainMembers = members.stream()
                .filter(id -> !ValidationUtils.equals(id, userId))
                .collect(toImmutableSet());
        if (notEquals(remainMembers, userId)) {
            raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        }

        this.members = remainMembers;
        addOpsLog("移除成员", userContext);
    }

    public void removeMembers(List<String> userIds, UserContext userContext) {
        this.managers = managers.stream().filter(id -> !userIds.contains(id)).collect(toImmutableSet());

        Set<String> remainMembers = members.stream().filter(id -> !userIds.contains(id)).collect(toImmutableSet());
        if (notEquals(members, remainMembers)) {
            raiseEvent(new GroupMembersChangedEvent(getId(), userIds, userContext));
        }

        this.members = remainMembers;
        addOpsLog("移除成员(多)", userContext);
    }

    public void addManager(String userId, UserContext userContext) {
        if (managers.contains(userId)) {
            return;
        }

        if (managers.size() + 1 > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值(" + MAX_GROUP_MANAGER_SIZE + "个)。", "groupId", this.getId());
        }

        if (!members.contains(userId)) {
            this.members.add(userId);
            raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        }

        this.managers.add(userId);

        addOpsLog("添加管理员", userContext);
    }

    public void addManagers(List<String> userIds, UserContext userContext) {
        if (managers.containsAll(userIds)) {
            return;
        }

        if (managers.size() + userIds.size() > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值(" + MAX_GROUP_MANAGER_SIZE + "个)。", "groupId", this.getId());
        }

        Set<String> resultMembers = Stream.concat(members.stream(), userIds.stream()).collect(toImmutableSet());
        if (notEquals(members, resultMembers)) {
            raiseEvent(new GroupMembersChangedEvent(getId(), userIds, userContext));
        }
        this.members = resultMembers;

        this.managers = Stream.concat(managers.stream(), userIds.stream()).collect(toImmutableSet());

        addOpsLog("添加管理员(多)", userContext);
    }

    public void removeManager(String userId, UserContext userContext) {
        if (!managers.contains(userId)) {
            return;
        }

        this.managers.remove(userId);
        addOpsLog("移除管理员", userContext);
    }

    public void removeManagers(List<String> userIds, UserContext userContext) {
        this.managers = managers.stream().filter(id -> !userIds.contains(id)).collect(toImmutableSet());
        addOpsLog("移除管理员(多)", userContext);
    }

    public void activate(UserContext userContext) {
        if (active) {
            return;
        }

        this.active = true;
        addOpsLog("启用", userContext);
    }

    public void deactivate(UserContext userContext) {
        if (!active) {
            return;
        }

        this.active = false;
        addOpsLog("禁用", userContext);
    }

    public boolean containsManager(String userId) {
        return this.managers.contains(userId);
    }

    public boolean containsMember(String userId) {
        return containsManager(userId) || this.members.contains(userId);
    }

    public Set<String> allManagerIds() {
        return managers;
    }

    public boolean appliesTo(String folderId) {
        return grants.containsKey(folderId);
    }

    /**
     * 获取当前文件夹的权限集合
     *
     * @param ancestors 当前文件夹的祖先集合，包括自身
     * @return 权限集合
     */
    public Set<Permission> permissionsOf(List<String> ancestors) {
        return PermissionInheritanceResolver.resolve(inheritancePolicy, grants, ancestors);
    }

    public void addGrant(String folderId, Set<Permission> permissions, UserContext userContext) {
        grants.put(folderId, new HashSet<>(permissions));
        addOpsLog("设置资源权限：" + folderId, userContext);
    }

    public void addGrants(List<String> folderIds, Set<Permission> permissions, UserContext userContext) {
        if (isEmpty(folderIds)) {
            return;
        }
        for (String folderId : folderIds) {
            grants.put(folderId, new HashSet<>(permissions));
        }
        addOpsLog("批量设置资源权限", userContext);
    }

    public boolean containsFolder(String folderId) {
        return grants.containsKey(folderId);
    }

    public void revoke(String folderId, UserContext userContext) {
        grants.remove(folderId);
        addOpsLog("移除资源权限：" + folderId, userContext);
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new GroupDeletedEvent(getCustomId(), userContext));
    }

}
