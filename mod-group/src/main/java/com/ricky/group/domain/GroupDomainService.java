package com.ricky.group.domain;

import com.ricky.common.auth.Permission;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Service
@RequiredArgsConstructor
public class GroupDomainService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FolderHierarchyDomainService folderHierarchyDomainService;

    /**
     * 是否具备 required 中的所有权限（单资源）
     */
    public boolean hasPermission(String userId, String customId, String folderId, Set<Permission> required) {
        Set<Permission> permissions = resolvePermissions(userId, customId, folderId);
        return permissions.containsAll(required);
    }

    /**
     * 是否在所有资源上都具备 required 中的所有权限
     */
    public boolean hasPermission(String userId, String customId, List<String> folderIds, Set<Permission> required) {
        return folderIds.stream()
                .allMatch(folderId -> hasPermission(userId, customId, folderId, required));
    }

    /**
     * 是否具备某一个权限
     */
    public boolean can(String userId, String customId, String folderId, Permission permission) {
        return resolvePermissions(userId, customId, folderId).contains(permission);
    }

    /**
     * 计算用户在某个资源上的最终权限集合
     */
    public Set<Permission> resolvePermissions(String userId, String customId, String folderId) {
        User user = userRepository.cachedById(userId);
        List<String> ancestors = folderHierarchyDomainService.withAllParentIdsOf(customId, folderId);

        return groupRepository.byIds(user.getGroupIds()).stream()
                .filter(Group::isActive)
                .filter(group -> group.containsMember(userId))
                .filter(group -> group.appliesTo(folderId))
                .flatMap(group -> group.permissionsOf(folderId, ancestors).stream())
                .collect(toImmutableSet());
    }

}
