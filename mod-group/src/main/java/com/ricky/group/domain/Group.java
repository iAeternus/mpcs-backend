package com.ricky.group.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.permission.Permission;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.group.domain.event.GroupDeletedEvent;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.domain.SpaceType.teamCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.MAX_GROUP_MANAGER_REACHED;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.group.domain.MemberRole.ADMIN;
import static com.ricky.group.domain.MemberRole.ORDINARY;

/**
 * 权限组，描述用户集合对资源集合的权限集合，用来映射真实团队组织结构。
 */
@Getter
@TypeAlias("group")
@Document(GROUP_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Group extends AggregateRoot {

    private String name; // 名称
    private boolean active; // 是否启用
    private List<Member> members; // 成员列表（含角色）
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
        this.members = new ArrayList<>();
        this.customId = teamCustomId(getId());
        this.grants = new HashMap<>();
        this.inheritancePolicy = InheritancePolicy.NONE;
        addOpsLog("新建", userContext);
    }

    public static String newGroupId() {
        return GROUP_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void rename(String newName, UserContext userContext) {
        if (ValidationUtils.equals(name, newName)) {
            return;
        }

        this.name = newName;
        addOpsLog("重命名为:" + name, userContext);
    }

    public void addMembers(List<String> userIds, UserContext userContext) {
        if (isEmpty(userIds)) {
            return;
        }

        Set<String> distinctUserIds = new HashSet<>(userIds);
        List<String> addedMemberIds = new ArrayList<>();
        for (String userId : distinctUserIds) {
            if (containsMember(userId)) {
                continue;
            }
            this.members.add(Member.builder()
                    .userId(userId)
                    .role(ORDINARY)
                    .joinedAt(Instant.now())
                    .build());
            addedMemberIds.add(userId);
        }

        if (!addedMemberIds.isEmpty()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), addedMemberIds, userContext));
        }

        addOpsLog("设置成员", userContext);
    }

    public void removeMember(String userId, UserContext userContext) {
        int beforeSize = members.size();
        this.members = members.stream()
                .filter(member -> !ValidationUtils.equals(member.getUserId(), userId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (beforeSize != members.size()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        }

        addOpsLog("移除成员", userContext);
    }

    public void removeMembers(List<String> userIds, UserContext userContext) {
        if (isEmpty(userIds)) {
            return;
        }

        Set<String> removedUserIds = new HashSet<>(userIds);
        int beforeSize = members.size();
        this.members = members.stream()
                .filter(member -> !removedUserIds.contains(member.getUserId()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (beforeSize != members.size()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), userIds, userContext));
        }

        addOpsLog("移除成员(批量)", userContext);
    }

    public void addManager(String userId, UserContext userContext) {
        if (containsManager(userId)) {
            return;
        }

        if (adminCount() + 1 > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值:" + MAX_GROUP_MANAGER_SIZE + "。", "groupId", this.getId());
        }

        int index = indexOfMember(userId);
        if (index >= 0) {
            promoteToAdmin(index);
            addOpsLog("添加管理员", userContext);
            return;
        }

        this.members.add(Member.builder()
                .userId(userId)
                .role(ADMIN)
                .joinedAt(Instant.now())
                .build());
        raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        addOpsLog("添加管理员", userContext);
    }

    public void addManagers(List<String> userIds, UserContext userContext) {
        if (isEmpty(userIds)) {
            return;
        }

        Set<String> distinctUserIds = new HashSet<>(userIds);
        long newAdmins = distinctUserIds.stream().filter(id -> !containsManager(id)).count();
        if (adminCount() + newAdmins > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "无法添加管理员，管理员数量已达所允许的最大值:" + MAX_GROUP_MANAGER_SIZE + "。", "groupId", this.getId());
        }

        List<String> addedMemberIds = new ArrayList<>();
        for (String userId : distinctUserIds) {
            if (containsManager(userId)) {
                continue;
            }

            int index = indexOfMember(userId);
            if (index >= 0) {
                promoteToAdmin(index);
                continue;
            }

            this.members.add(Member.builder()
                    .userId(userId)
                    .role(ADMIN)
                    .joinedAt(Instant.now())
                    .build());
            addedMemberIds.add(userId);
        }

        if (!addedMemberIds.isEmpty()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), addedMemberIds, userContext));
        }

        addOpsLog("添加管理员(批量)", userContext);
    }

    public void removeManager(String userId, UserContext userContext) {
        int index = indexOfMember(userId);
        if (index < 0) {
            return;
        }

        Member member = members.get(index);
        if (member.getRole() != ADMIN) {
            return;
        }

        demoteToNormal(index);
        addOpsLog("移除管理员", userContext);
    }

    public void removeManagers(List<String> userIds, UserContext userContext) {
        if (isEmpty(userIds)) {
            return;
        }

        Set<String> demoteUserIds = new HashSet<>(userIds);
        boolean changed = false;
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            if (!demoteUserIds.contains(member.getUserId())) {
                continue;
            }
            if (member.getRole() == ADMIN) {
                demoteToNormal(i);
                changed = true;
            }
        }

        if (changed) {
            addOpsLog("移除管理员(批量)", userContext);
        }
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
        return members.stream()
                .anyMatch(member -> ValidationUtils.equals(member.getUserId(), userId) && member.getRole() == ADMIN);
    }

    public boolean containsMember(String userId) {
        return members.stream()
                .anyMatch(member -> ValidationUtils.equals(member.getUserId(), userId));
    }

    public Set<String> allManagerIds() {
        return members.stream()
                .filter(member -> member.getRole() == ADMIN)
                .map(Member::getUserId)
                .collect(toImmutableSet());
    }

    public boolean appliesTo(String folderId) {
        return grants.containsKey(folderId);
    }

    /**
     * 获取当前文件夹的权限集合。
     *
     * @param ancestors 当前文件夹的祖先集合，包含自身
     * @return 权限集合
     */
    public Set<Permission> permissionsOf(List<String> ancestors) {
        return PermissionInheritanceResolver.resolve(inheritancePolicy, grants, ancestors);
    }

    public void addGrant(String folderId, Set<Permission> permissions, UserContext userContext) {
        grants.put(folderId, new HashSet<>(permissions));
        addOpsLog("设置资源权限:" + folderId, userContext);
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
        addOpsLog("移除资源权限:" + folderId, userContext);
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new GroupDeletedEvent(getCustomId(), userContext));
    }

    private int indexOfMember(String userId) {
        for (int i = 0; i < members.size(); i++) {
            if (ValidationUtils.equals(members.get(i).getUserId(), userId)) {
                return i;
            }
        }
        return -1;
    }

    private void promoteToAdmin(int index) {
        Member member = members.get(index);
        members.set(index, Member.builder()
                .userId(member.getUserId())
                .role(ADMIN)
                .joinedAt(member.getJoinedAt())
                .build());
    }

    private void demoteToNormal(int index) {
        Member member = members.get(index);
        members.set(index, Member.builder()
                .userId(member.getUserId())
                .role(ORDINARY)
                .joinedAt(member.getJoinedAt())
                .build());
    }

    private long adminCount() {
        return members.stream().filter(member -> member.getRole() == ADMIN).count();
    }
}
