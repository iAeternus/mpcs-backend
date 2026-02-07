package com.ricky.group.domain;

import com.ricky.common.domain.SpaceType;
import com.ricky.common.permission.Permission;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.folder.domain.FolderDomainService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.domain.SpaceType.TEAM;
import static com.ricky.common.exception.ErrorCodeEnum.GROUP_WITH_NAME_ALREADY_EXISTS;
import static com.ricky.common.exception.ErrorCodeEnum.NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class GroupDomainService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FolderDomainService folderDomainService;

    /**
     * 是否具备 required 中的所有权限（单资源）
     */
    public boolean hasPermission(String userId, String customId, String folderId, Set<Permission> required) {
        if(notTeamSpace(customId)) {
            return true;
        }

        Set<Permission> permissions = resolvePermissions(userId, customId, folderId);
        return permissions.containsAll(required);
    }

    /**
     * 是否在所有资源上都具备 required 中的所有权限
     */
    public boolean hasPermission(String userId, String customId, List<String> folderIds, Set<Permission> required) {
        if(notTeamSpace(customId)) {
            return true;
        }

        return folderIds.stream()
                .allMatch(folderId -> hasPermission(userId, customId, folderId, required));
    }

    /**
     * 是否具备某一个权限
     */
    public boolean can(String userId, String customId, String folderId, Permission permission) {
        if(notTeamSpace(customId)) {
            return true;
        }

        return resolvePermissions(userId, customId, folderId).contains(permission);
    }

    /**
     * 计算用户在某个资源上的最终权限集合
     */
    public Set<Permission> resolvePermissions(String userId, String customId, String folderId) {
        User user = userRepository.cachedById(userId);
        List<String> ancestors = folderDomainService.withAllParentIdsRev(customId, folderId);

        return groupRepository.byIds(user.getGroupIds()).stream()
                .filter(Group::isActive)
                .filter(group -> group.containsMember(userId))
                .filter(group -> group.appliesTo(folderId))
                .flatMap(group -> group.permissionsOf(ancestors).stream())
                .collect(toImmutableSet());
    }

    private boolean notTeamSpace(String customId) {
        return SpaceType.fromCustomId(customId) != TEAM;
    }

    public void rename(Group group, String newName, UserContext userContext) {
        if (groupRepository.cachedExistsByName(newName, userContext.getUid())) {
            throw new MyException(GROUP_WITH_NAME_ALREADY_EXISTS, "重命名失败，名称已被占用。",
                    "groupId", group.getId(), "name", newName);
        }

        group.rename(newName, userContext);
    }

    public void checkDeleteGroups(User user, Set<String> tobeDeletedGroupIds) {
        checkAtLeastOneVisibleGroupExists(user, tobeDeletedGroupIds, "删除");
    }

    public void checkDeactivateGroups(User user, Set<String> tobeDeactivatedGroupIds) {
        checkAtLeastOneVisibleGroupExists(user, tobeDeactivatedGroupIds, "禁用");
    }

    private void checkAtLeastOneVisibleGroupExists(User user, Set<String> excludedGroupIds, String ops) {
        Set<String> remainActiveGroupIds = groupRepository.cachedUserAllGroups(user.getId()).stream()
                .filter(group -> !excludedGroupIds.contains(group.getId()))
                .filter(UserCachedGroup::isVisible)
                .map(UserCachedGroup::getId)
                .collect(toImmutableSet());

        if (isEmpty(remainActiveGroupIds)) {
            throw new MyException(NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT,
                    ops + "失败，必须保留至少一个可见（非禁用）的权限组。",
                    "userId", user.getId());
        }
    }
}
