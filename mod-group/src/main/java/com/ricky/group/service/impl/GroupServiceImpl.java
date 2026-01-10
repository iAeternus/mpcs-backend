package com.ricky.group.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.group.command.AddGroupManagersCommand;
import com.ricky.group.command.AddGroupMembersCommand;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.command.RenameGroupCommand;
import com.ricky.group.domain.*;
import com.ricky.group.service.GroupService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserDomainService;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final RateLimiter rateLimiter;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupDomainService groupDomainService;
    private final UserDomainService userDomainService;
    private final GroupFactory groupFactory;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // TODO 谁有权限创建组还没有实现
    @Override
    @Transactional
    public String createGroup(CreateGroupCommand command, UserContext userContext) {
        rateLimiter.applyFor("Group:CreateGroup", 10);

        Group group = groupFactory.create(command.getName(), userContext);
        group.addManager(userContext.getUid(), userContext);
        groupRepository.save(group);

        User user = userRepository.byId(userContext.getUid());
        user.addGroup(group.getId(), userContext);
        userRepository.save(user);

        log.info("Created Group[{}]", group.getId());
        return group.getId();
    }

    @Override
    @Transactional
    public void renameGroup(String groupId, RenameGroupCommand command, UserContext userContext) {
        rateLimiter.applyFor("Group:RenameGroup", 10);

        Group group = groupRepository.byId(groupId);
        if (ValidationUtils.equals(group.getName(), command.getNewName())) {
            return;
        }

        managePermissionChecker.checkCanManageGroup(group, userContext);
        groupDomainService.rename(group, command.getNewName(), userContext);
        groupRepository.save(group);

        log.info("Renamed Group[{}]", group.getId());
    }

    @Override
    @Transactional
    public void addGroupMembers(String groupId, AddGroupMembersCommand command, UserContext userContext) {
        rateLimiter.applyFor("Group:AddGroupMembers", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        userDomainService.checkAllUsersExists(command.getMemberIds());

        group.addMembers(command.getMemberIds(), userContext);
        groupRepository.save(group);

        log.info("Added members{} to group[{}].", command.getMemberIds(), groupId);
    }

    @Override
    @Transactional
    public void addGroupManagers(String groupId, AddGroupManagersCommand command, UserContext userContext) {
        rateLimiter.applyFor("Group:AddGroupManagers", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        userDomainService.checkAllUsersExists(command.getManagerIds());

        group.addManagers(command.getManagerIds(), userContext);
        groupRepository.save(group);

        log.info("Added managers{} to group[{}].", command.getManagerIds(), groupId);
    }

    @Override
    @Transactional
    public void removeGroupMember(String groupId, String memberId, UserContext userContext) {
        rateLimiter.applyFor("Group:RemoveGroupMember", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        group.removeMember(memberId, userContext);
        groupRepository.save(group);

        log.info("Removed member[{}] from group[{}].", memberId, groupId);
    }

    @Override
    @Transactional
    public void addGroupManager(String groupId, String memberId, UserContext userContext) {
        rateLimiter.applyFor("Group:AddGroupManager", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        group.addManager(memberId, userContext);
        groupRepository.save(group);

        log.info("Added manager[{}] to group[{}].", memberId, groupId);
    }

    @Override
    @Transactional
    public void removeGroupManager(String groupId, String memberId, UserContext userContext) {
        rateLimiter.applyFor("Group:RemoveGroupManager", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        group.removeManager(memberId, userContext);
        groupRepository.save(group);

        log.info("Removed manager[{}] from group[{}].", memberId, groupId);
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:DeleteGroup", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);

        User user = userRepository.byId(userContext.getUid());
        groupDomainService.checkDeleteGroups(user, Set.of(groupId));

        group.onDelete(userContext);
        groupRepository.delete(group);

        user.removeGroup(groupId, userContext);
        userRepository.save(user);

        log.info("Deleted group[{}].", groupId);
    }

    @Override
    @Transactional
    public void activateGroup(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:ActivateGroup", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);
        group.activate(userContext);
        groupRepository.save(group);

        log.info("Archived group[{}].", groupId);
    }

    @Override
    @Transactional
    public void deactivateGroup(String groupId, UserContext userContext) {
        rateLimiter.applyFor("Group:DeactivateGroup", 10);

        Group group = groupRepository.byId(groupId);
        managePermissionChecker.checkCanManageGroup(group, userContext);

        User user = userRepository.cachedById(userContext.getUid());
        groupDomainService.checkDeactivateGroups(user, Set.of(groupId));

        group.deactivate(userContext);
        groupRepository.save(group);

        log.info("Deactivated group[{}].", groupId);
    }
}
