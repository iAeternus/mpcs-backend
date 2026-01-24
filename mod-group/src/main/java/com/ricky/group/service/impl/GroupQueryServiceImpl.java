package com.ricky.group.service.impl;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.Pagination;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.group.domain.CachedGroup;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.GroupRepository;
import com.ricky.group.query.*;
import com.ricky.group.service.GroupQueryService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.GROUP_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.GROUP_ID_PREFIX;
import static com.ricky.common.utils.MongoCriteriaUtils.regexSearch;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static com.ricky.common.validation.id.IdValidator.isId;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class GroupQueryServiceImpl implements GroupQueryService {

    private final static Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "createdAt", "active");
    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final GroupRepository groupRepository;
    private final FolderRepository folderRepository;
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
        Set<String> managerIds = group.getManagers();
        Set<String> memberIds = group.getMembers();

        Set<String> ordinaryMemberIds = memberIds.stream()
                .filter(memberId -> !managerIds.contains(memberId)) // 过滤出普通成员
                .collect(toImmutableSet());

        List<User> users = userRepository.byIds(ordinaryMemberIds);

        List<GroupOrdinaryMembersResponse.OrdinaryMember> ordinaryMembers = users.stream()
                .map(user -> GroupOrdinaryMembersResponse.OrdinaryMember.builder()
                        .username(user.getUsername())
                        .mobileOrEmail(user.getMobileOrEmail())
                        .build())
                .collect(toImmutableList());

        return GroupOrdinaryMembersResponse.builder()
                .groupOrdinaryMembers(ordinaryMembers)
                .build();
    }

    @Override
    public GroupManagersResponse fetchGroupManagers(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:FetchGroupManagers", 50);

        CachedGroup group = groupRepository.cachedById(groupId);
        Set<String> managerIds = group.getManagers();

        List<User> managers = userRepository.byIds(managerIds);
        List<GroupManagersResponse.Manager> groupManagers = managers.stream()
                .map(user -> GroupManagersResponse.Manager.builder()
                        .username(user.getUsername())
                        .mobileOrEmail(user.getMobileOrEmail())
                        .build())
                .collect(toImmutableList());

        return GroupManagersResponse.builder()
                .groupManagers(groupManagers)
                .build();
    }

    @Override
    public PagedList<GroupResponse> pageMyGroupsAsForManager(MyGroupsAsForManaberPageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("Group:PageMyGroupFolders", 5);

        String search = pageQuery.getSearch();
        Pagination pagination = Pagination.pagination(pageQuery.getPageIndex(), pageQuery.getPageSize());

        Query query = query(where("managers").is(userContext.getUid())); // my managed

        if (isNotBlank(search)) {
            if (isId(search, GROUP_ID_PREFIX)) {
                query.addCriteria(where("_id").is(search));
            } else {
                query.addCriteria(regexSearch("name", search));
            }
        }

        long count = mongoTemplate.count(query, Group.class);
        if (count == 0) {
            return PagedList.pagedList(pagination, (int) count, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(pageQuery));
        query.fields().include("_id", "name", "active", "inheritancePolicy", "createdAt", "updatedAt");
        List<GroupResponse> myGroups = mongoTemplate.find(query, GroupResponse.class, GROUP_COLLECTION);
        return PagedList.pagedList(pagination, (int) count, myGroups);
    }

    private Sort sort(MyGroupsAsForManaberPageQuery pageQuery) {
        String sortedBy = pageQuery.getSortedBy();
        if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = pageQuery.getAscSort() ? ASC : DESC;
        if (ValidationUtils.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        return by(direction, sortedBy).and(by(DESC, "createdAt"));
    }

    @Override
    public PagedList<GroupResponse> pageMyGroupsAsForMember(MyGroupsAsForMemberPageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("Group:PageMyGroupFolders", 5);

        String search = pageQuery.getSearch();
        Pagination pagination = Pagination.pagination(pageQuery.getPageIndex(), pageQuery.getPageSize());

        Query query = query(where("members").is(userContext.getUid())); // my joined

        if (isNotBlank(search)) {
            if (isId(search, GROUP_ID_PREFIX)) {
                query.addCriteria(where("_id").is(search));
            } else {
                query.addCriteria(regexSearch("name", search));
            }
        }

        long count = mongoTemplate.count(query, Group.class);
        if (count == 0) {
            return PagedList.pagedList(pagination, (int) count, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(pageQuery));
        query.fields().include("_id", "name", "active", "inheritancePolicy", "createdAt", "updatedAt");
        List<GroupResponse> myGroups = mongoTemplate.find(query, GroupResponse.class, GROUP_COLLECTION);
        return PagedList.pagedList(pagination, (int) count, myGroups);
    }

    private Sort sort(MyGroupsAsForMemberPageQuery pageQuery) {
        String sortedBy = pageQuery.getSortedBy();
        if (isBlank(sortedBy) || !ALLOWED_SORT_FIELDS.contains(sortedBy)) {
            return by(DESC, "createdAt");
        }

        Sort.Direction direction = pageQuery.getAscSort() ? ASC : DESC;
        if (ValidationUtils.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        return by(direction, sortedBy).and(by(DESC, "createdAt"));
    }
}
