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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.GROUP_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.GROUP_ID_PREFIX;
import static com.ricky.common.constants.ConfigConstants.MAX_GROUP_MANAGER_SIZE;
import static com.ricky.common.domain.SpaceType.teamCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.ACCESS_DENIED;
import static com.ricky.common.exception.ErrorCodeEnum.MAX_GROUP_MANAGER_REACHED;
import static com.ricky.common.exception.ErrorCodeEnum.NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.group.domain.MemberRole.ADMIN;
import static com.ricky.group.domain.MemberRole.ORDINARY;

@Getter
@TypeAlias("group")
@Document(GROUP_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Group extends AggregateRoot {

    private String name;
    private boolean active;
    private List<Member> members;
    private String customId;
    private List<MemberAuthorization> memberAuthorizations;
    private Map<String, Set<Permission>> grants;
    private InheritancePolicy inheritancePolicy;

    public Group(String name, UserContext userContext) {
        super(newGroupId(), userContext);
        init(name, userContext);
    }

    private void init(String name, UserContext userContext) {
        this.name = name;
        this.active = true;
        this.members = new ArrayList<>();
        this.customId = teamCustomId(getId());
        this.memberAuthorizations = new ArrayList<>();
        this.grants = new HashMap<>();
        this.inheritancePolicy = InheritancePolicy.NONE;
        addOpsLog("create-group", userContext);
    }

    public static String newGroupId() {
        return GROUP_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void rename(String newName, UserContext userContext) {
        if (ValidationUtils.equals(name, newName)) {
            return;
        }

        this.name = newName;
        addOpsLog("rename-group:" + name, userContext);
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

        addOpsLog("add-members", userContext);
    }

    public void removeMember(String userId, UserContext userContext) {
        int beforeSize = members.size();
        this.members = members.stream()
                .filter(member -> !ValidationUtils.equals(member.getUserId(), userId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        removeAuthorization(userId);

        if (beforeSize != members.size()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        }

        addOpsLog("remove-member", userContext);
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
        removedUserIds.forEach(this::removeAuthorization);

        if (beforeSize != members.size()) {
            raiseEvent(new GroupMembersChangedEvent(getId(), userIds, userContext));
        }

        addOpsLog("remove-members", userContext);
    }

    public void addManager(String userId, UserContext userContext) {
        if (containsManager(userId)) {
            return;
        }

        if (adminCount() + 1 > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "max group manager reached: " + MAX_GROUP_MANAGER_SIZE, "groupId", this.getId());
        }

        int index = indexOfMember(userId);
        if (index >= 0) {
            promoteToAdmin(index);
            removeAuthorization(userId);
            addOpsLog("add-manager", userContext);
            return;
        }

        this.members.add(Member.builder()
                .userId(userId)
                .role(ADMIN)
                .joinedAt(Instant.now())
                .build());
        raiseEvent(new GroupMembersChangedEvent(getId(), List.of(userId), userContext));
        addOpsLog("add-manager", userContext);
    }

    public void addManagers(List<String> userIds, UserContext userContext) {
        if (isEmpty(userIds)) {
            return;
        }

        Set<String> distinctUserIds = new HashSet<>(userIds);
        long newAdmins = distinctUserIds.stream().filter(id -> !containsManager(id)).count();
        if (adminCount() + newAdmins > MAX_GROUP_MANAGER_SIZE) {
            throw new MyException(MAX_GROUP_MANAGER_REACHED,
                    "max group manager reached: " + MAX_GROUP_MANAGER_SIZE, "groupId", this.getId());
        }

        List<String> addedMemberIds = new ArrayList<>();
        for (String userId : distinctUserIds) {
            if (containsManager(userId)) {
                continue;
            }

            int index = indexOfMember(userId);
            if (index >= 0) {
                promoteToAdmin(index);
                removeAuthorization(userId);
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

        addOpsLog("add-managers", userContext);
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
        addOpsLog("remove-manager", userContext);
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
            addOpsLog("remove-managers", userContext);
        }
    }

    public void activate(UserContext userContext) {
        if (active) {
            return;
        }

        this.active = true;
        addOpsLog("activate-group", userContext);
    }

    public void deactivate(UserContext userContext) {
        if (!active) {
            return;
        }

        this.active = false;
        addOpsLog("deactivate-group", userContext);
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

    public Set<Permission> permissionsOf(List<String> ancestors) {
        return PermissionInheritanceResolver.resolve(inheritancePolicy, grants, ancestors);
    }

    public boolean hasExplicitAuthorization(String userId) {
        return authorizationOf(userId).isPresent();
    }

    public Map<String, Set<Permission>> grantsOf(String userId) {
        return authorizationOf(userId)
                .map(MemberAuthorization::getGrants)
                .orElse(grants);
    }

    public InheritancePolicy inheritancePolicyOf(String userId) {
        return authorizationOf(userId)
                .map(MemberAuthorization::getInheritancePolicy)
                .orElse(inheritancePolicy);
    }

    public boolean appliesTo(String userId, String folderId) {
        Map<String, Set<Permission>> resolvedGrants = grantsOf(userId);
        return resolvedGrants != null && resolvedGrants.containsKey(folderId);
    }

    public Set<Permission> permissionsOf(String userId, List<String> ancestors) {
        return authorizationOf(userId)
                .map(authorization -> authorization.permissionsOf(ancestors))
                .orElseGet(() -> PermissionInheritanceResolver.resolve(inheritancePolicy, grants, ancestors));
    }

    public void addGrant(String targetMemberId, String folderId, Set<Permission> permissions,
                         InheritancePolicy policy, UserContext userContext) {
        ensureAuthorizableOrdinaryMember(targetMemberId);
        upsertAuthorization(targetMemberId, policy, authorization -> authorization.grant(folderId, permissions));
        addOpsLog("grant-member-folder:" + targetMemberId + ":" + folderId, userContext);
    }

    public void addGrants(String targetMemberId, List<String> folderIds, Set<Permission> permissions,
                          InheritancePolicy policy, UserContext userContext) {
        if (isEmpty(folderIds)) {
            return;
        }

        ensureAuthorizableOrdinaryMember(targetMemberId);
        upsertAuthorization(targetMemberId, policy, authorization -> authorization.grant(folderIds, permissions));
        addOpsLog("grant-member-folders:" + targetMemberId, userContext);
    }

    public boolean containsFolder(String folderId) {
        return grants.containsKey(folderId);
    }

    public void revoke(String folderId, UserContext userContext) {
        grants.remove(folderId);
        addOpsLog("revoke-folder:" + folderId, userContext);
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

    private Optional<MemberAuthorization> authorizationOf(String userId) {
        if (ValidationUtils.isEmpty(memberAuthorizations)) {
            return Optional.empty();
        }
        return memberAuthorizations.stream()
                .filter(authorization -> ValidationUtils.equals(authorization.getUserId(), userId))
                .findFirst();
    }

    private void upsertAuthorization(String userId, InheritancePolicy policy,
                                     Function<MemberAuthorization, MemberAuthorization> updater) {
        MemberAuthorization base = authorizationOf(userId)
                .map(existing -> existing.toBuilder().inheritancePolicy(policy).build())
                .orElse(MemberAuthorization.builder()
                        .userId(userId)
                        .inheritancePolicy(policy)
                        .build());
        MemberAuthorization updated = updater.apply(base);
        removeAuthorization(userId);
        memberAuthorizations.add(updated);
    }

    private void removeAuthorization(String userId) {
        if (memberAuthorizations == null) {
            memberAuthorizations = new ArrayList<>();
            return;
        }
        memberAuthorizations = memberAuthorizations.stream()
                .filter(authorization -> !ValidationUtils.equals(authorization.getUserId(), userId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void ensureAuthorizableOrdinaryMember(String userId) {
        int index = indexOfMember(userId);
        if (index < 0) {
            throw new MyException(NOT_FOUND, "member not found in group", "memberId", userId, "groupId", getId());
        }

        if (members.get(index).getRole() == ADMIN) {
            throw new MyException(ACCESS_DENIED, "admin has implicit full permissions", "memberId", userId, "groupId", getId());
        }
    }
}
