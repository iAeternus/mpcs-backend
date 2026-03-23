package com.ricky.group.service.impl;

import com.ricky.common.domain.page.MongoPageQuery;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderDomainService;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.group.domain.*;
import com.ricky.group.query.*;
import com.ricky.group.service.GroupQueryService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.GROUP_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.GROUP_ID_PREFIX;
import static com.ricky.common.domain.SpaceType.TEAM;
import static com.ricky.common.permission.Permission.READ;
import static com.ricky.common.permission.Permission.all;
import static com.ricky.common.utils.MongoCriteriaUtils.regexSearch;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.validation.id.IdValidator.isId;
import static com.ricky.group.domain.MemberRole.ADMIN;
import static com.ricky.group.domain.MemberRole.ORDINARY;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import com.ricky.common.domain.SpaceType;
import com.ricky.common.permission.Permission;

@Service
@RequiredArgsConstructor
public class GroupQueryServiceImpl implements GroupQueryService {

    private final static Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt", "active");
    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final GroupRepository groupRepository;
    private final FolderRepository folderRepository;
    private final FolderDomainService folderDomainService;
    private final UserRepository userRepository;

    @Override
    public GroupFoldersResponse fetchGroupFolders(String groupId) {
        rateLimiter.applyFor("Group:FetchGroupFolders", 50);

        CachedGroup group = groupRepository.cachedById(groupId);
        List<Folder> folders = folderRepository.byIds(group.getGrants().keySet());

        List<GroupFoldersResponse.GroupFolder> groupFolders = folders.stream()
                .map(folder -> GroupFoldersResponse.GroupFolder.builder()
                        .folderId(folder.getId())
                        .folderName(folder.getFolderName())
                        .build())
                .collect(toImmutableList());

        return GroupFoldersResponse.builder()
                .groupFolders(groupFolders)
                .build();
    }

    @Override
    public GroupOrdinaryMembersResponse fetchGroupOrdinaryMembers(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:FetchGroupMembers", 50);

        CachedGroup group = groupRepository.cachedById(groupId);
        List<Member> members = group.getMembers() == null ? List.of() : group.getMembers();
        Set<String> ordinaryMemberIds = members.stream()
                .filter(member -> member.getRole() == ORDINARY)
                .map(Member::getUserId)
                .collect(toImmutableSet());

        Map<String, Member> memberById = members.stream()
                .filter(member -> member.getRole() == ORDINARY)
                .collect(toMap(Member::getUserId, member -> member, (left, right) -> left));
        List<User> users = userRepository.byIds(ordinaryMemberIds);

        List<GroupOrdinaryMembersResponse.OrdinaryMember> ordinaryMembers = users.stream()
                .map(user -> {
                    Member member = memberById.get(user.getId());
                    return GroupOrdinaryMembersResponse.OrdinaryMember.builder()
                            .username(user.getUsername())
                            .mobileOrEmail(user.getMobileOrEmail())
                            .joinedAt(member == null ? null : member.getJoinedAt())
                            .build();
                })
                .collect(toImmutableList());

        return GroupOrdinaryMembersResponse.builder()
                .groupOrdinaryMembers(ordinaryMembers)
                .build();
    }

    @Override
    public GroupManagersResponse fetchGroupManagers(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:FetchGroupManagers", 50);

        CachedGroup group = groupRepository.cachedById(groupId);
        List<Member> members = group.getMembers() == null ? List.of() : group.getMembers();
        Set<String> managerIds = members.stream()
                .filter(member -> member.getRole() == MemberRole.ADMIN)
                .map(Member::getUserId)
                .collect(toImmutableSet());

        Map<String, Member> managerById = members.stream()
                .filter(member -> member.getRole() == MemberRole.ADMIN)
                .collect(toMap(Member::getUserId, member -> member, (left, right) -> left));
        List<User> managers = userRepository.byIds(managerIds);
        List<GroupManagersResponse.Manager> groupManagers = managers.stream()
                .map(user -> {
                    Member member = managerById.get(user.getId());
                    return GroupManagersResponse.Manager.builder()
                            .username(user.getUsername())
                            .mobileOrEmail(user.getMobileOrEmail())
                            .joinedAt(member == null ? null : member.getJoinedAt())
                            .build();
                })
                .collect(toImmutableList());

        return GroupManagersResponse.builder()
                .groupManagers(groupManagers)
                .build();
    }

    @Override
    public PagedList<GroupResponse> pageMyGroupsAsForManager(MyGroupsAsForManagerPageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("Group:PageMyGroupFolders", 5);

        return MongoPageQuery.of(Group.class, GROUP_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and("members").elemMatch(where("userId").is(userContext.getUid())
                        .and("role").is(MemberRole.ADMIN))) // 我管理的
                .search((search, c, q) -> {
                    if (isId(search, GROUP_ID_PREFIX)) {
                        return c.and("_id").is(search);
                    } else {
                        return c.andOperator(regexSearch("name", search));
                    }
                })
                .sort(q -> {
                    String sortedBy = pageQuery.getSortedBy();
                    if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
                        return by(DESC, "createdAt");
                    }

                    var direction = pageQuery.getAscSort() ? ASC : DESC;
                    if (ValidationUtils.equals(sortedBy, "createdAt")) {
                        return by(direction, "createdAt");
                    }

                    return by(direction, sortedBy).and(by(DESC, "createdAt"));
                })
                .project("_id", "name", "active", "customId", "inheritancePolicy", "createdAt", "updatedAt")
                .fetchAs(GroupResponse.class, mongoTemplate);
    }

    @Override
    public PagedList<GroupResponse> pageMyGroupsAsForMember(MyGroupsAsForMemberPageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("Group:PageMyGroupFolders", 5);

        return MongoPageQuery.of(Group.class, GROUP_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and("members").elemMatch(where("userId").is(userContext.getUid()))) // 我加入的
                .search((search, c, q) -> {
                    if (isId(search, GROUP_ID_PREFIX)) {
                        return c.and("_id").is(search);
                    } else {
                        return c.andOperator(regexSearch("name", search));
                    }
                })
                .sort(q -> {
                    String sortedBy = pageQuery.getSortedBy();
                    if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
                        return by(DESC, "createdAt");
                    }

                    Sort.Direction direction = pageQuery.getAscSort() ? ASC : DESC;
                    if (ValidationUtils.equals(sortedBy, "createdAt")) {
                        return by(direction, "createdAt");
                    }

                    return by(direction, sortedBy).and(by(DESC, "createdAt"));
                })
                .project("_id", "name", "active", "customId", "inheritancePolicy", "createdAt", "updatedAt")
                .fetchAs(GroupResponse.class, mongoTemplate);
    }

    @Override
    public FolderPermissionResponse fetchAdminPermission(String customId, String folderId) {
        rateLimiter.applyFor("Group:FetchAdminPermission", 50);

        if (SpaceType.fromCustomId(customId) != TEAM) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of())
                    .roleType("ADMIN")
                    .inherited(false)
                    .build();
        }

        List<String> ancestors = folderDomainService.withAllParentIdsRev(customId, folderId);
        CachedGroup cachedGroup = groupRepository.cachedByCustomId(customId);

        if (cachedGroup == null || !cachedGroup.isActive()) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of())
                    .roleType("ADMIN")
                    .inherited(false)
                    .build();
        }

        Set<Permission> permissions = all();
        boolean inherited = ancestors.size() > 1;

        return FolderPermissionResponse.builder()
                .folderId(folderId)
                .customId(customId)
                .permissions(permissions)
                .roleType("ADMIN")
                .inherited(inherited)
                .build();
    }

    @Override
    public FolderPermissionResponse fetchMemberPermission(String customId, String folderId) {
        rateLimiter.applyFor("Group:FetchMemberPermission", 50);

        if (SpaceType.fromCustomId(customId) != TEAM) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of(READ))
                    .roleType("ORDINARY")
                    .inherited(false)
                    .build();
        }

        List<String> ancestors = folderDomainService.withAllParentIdsRev(customId, folderId);
        CachedGroup cachedGroup = groupRepository.cachedByCustomId(customId);

        if (cachedGroup == null || !cachedGroup.isActive()) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of())
                    .roleType("ORDINARY")
                    .inherited(false)
                    .build();
        }

        Map<String, Set<Permission>> grants = cachedGroup.getGrants();
        if (grants == null || grants.isEmpty()) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of(READ))
                    .roleType("ORDINARY")
                    .inherited(false)
                    .build();
        }

        boolean appliesTo = grants.containsKey(folderId);
        if (!appliesTo) {
            return FolderPermissionResponse.builder()
                    .folderId(folderId)
                    .customId(customId)
                    .permissions(Set.of())
                    .roleType("ORDINARY")
                    .inherited(false)
                    .build();
        }

        Set<Permission> permissions = PermissionInheritanceResolver.resolve(
                cachedGroup.getInheritancePolicy(), grants, ancestors);
        boolean inherited = ancestors.size() > 1 && permissions.size() > grants.getOrDefault(folderId, Set.of()).size();

        return FolderPermissionResponse.builder()
                .folderId(folderId)
                .customId(customId)
                .permissions(permissions)
                .roleType("ORDINARY")
                .inherited(inherited)
                .build();
    }
}
