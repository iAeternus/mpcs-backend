package com.ricky.apitest.group;

import com.ricky.apitest.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.permission.Permission;
import com.ricky.folder.domain.Folder;
import com.ricky.group.command.*;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.event.GroupDeletedEvent;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import com.ricky.group.query.GroupFoldersResponse;
import com.ricky.group.query.GroupResponse;
import com.ricky.group.query.MyGroupsAsForManagerPageQuery;
import com.ricky.group.query.MyGroupsAsForMemberPageQuery;
import com.ricky.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.apitest.RandomTestFixture.rGroupName;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.event.DomainEventType.GROUP_DELETED;
import static com.ricky.common.event.DomainEventType.GROUP_MEMBERS_CHANGED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.permission.Permission.READ;
import static com.ricky.common.permission.Permission.WRITE;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.*;

public class GroupControllerTest extends BaseApiTest {

    @Test
    void should_create_group() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String groupName = rGroupName();

        // When
        String groupId = GroupApi.createGroup(loginResponse.getJwt(), CreateGroupCommand.builder()
                .name(groupName)
                .build());

        // Then
        Group group = groupRepository.byId(groupId);
        assertEquals(groupId, group.getId());
        assertEquals(groupName, group.getName());

        User user = userRepository.byId(loginResponse.getUserId());
        assertTrue(user.containsGroup(groupId));

        assertTrue(folderRepository.existsRoot(group.getCustomId()));
        Folder root = folderRepository.getRoot(group.getCustomId());
        assertEquals(groupName, root.getFolderName());
    }

    @Test
    void group_creator_should_also_be_group_manager() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();

        // When
        String groupId = GroupApi.createGroup(loginResponse.getJwt());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(loginResponse.getUserId()));
    }

    @Test
    void should_fail_to_create_group_if_name_already_exists() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String groupName = rGroupName();
        GroupApi.createGroup(loginResponse.getJwt(), groupName);

        // When & Then
        assertError(() -> GroupApi.createGroupRaw(loginResponse.getJwt(), CreateGroupCommand.builder()
                .name(groupName)
                .build()), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

//    @Test
//    void should_fail_to_create_group_if_custom_id_already_exists() {
//        // Given
//        LoginResponse loginResponse = setupApi.registerWithLogin();
//
//        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).customId(rCustomId()).build();
//        GroupApi.createGroup(loginResponse.getJwt(), command);
//
//        // When & Then
//        assertError(() -> GroupApi.createGroupRaw(loginResponse.getJwt(), command), FOLDER_HIERARCHY_WITH_CUSTOM_ID_ALREADY_EXISTS);
//    }

    @Test
    void should_rename_group() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(loginResponse.getJwt());
        String newName = rGroupName();

        // When
        GroupApi.renameGroup(loginResponse.getJwt(), groupId, newName);

        // Then
        Group group = groupRepository.byId(groupId);
        assertEquals(newName, group.getName());
    }

    @Test
    void should_fail_rename_group_if_name_already_exist() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String name = rGroupName();
        String groupId1 = GroupApi.createGroup(loginResponse.getJwt(), name);
        String groupId2 = GroupApi.createGroup(loginResponse.getJwt(), rGroupName());

        // When & Then
        assertError(() -> GroupApi.renameGroupRaw(loginResponse.getJwt(), groupId2, RenameGroupCommand.builder()
                .newName(name)
                .build()), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    void common_member_should_fail_rename_group() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        // When & Then
        assertError(() -> GroupApi.renameGroupRaw(member.getJwt(), groupId, RenameGroupCommand.builder()
                .newName(rGroupName())
                .build()), ACCESS_DENIED);
    }

    @Test
    void should_add_group_members() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        // When
        GroupApi.addGroupMembers(manager.getJwt(), groupId, AddGroupMembersCommand.builder()
                .memberIds(List.of(member1.getUserId(), member2.getUserId()))
                .build());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsMember(member1.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));
    }

    @Test
    void should_fail_add_group_members_if_not_all_members_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        // When & Then
        assertError(() -> GroupApi.addGroupMembersRaw(manager.getJwt(), groupId, AddGroupMembersCommand.builder()
                .memberIds(List.of(member.getUserId(), User.newUserId()))
                .build()), NOT_ALL_USERS_EXIST);
    }

    @Test
    void should_remove_member_from_group() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member1.getUserId(), member2.getUserId());
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsMember(member1.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));

        // When
        GroupApi.removeGroupMember(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group dbGroup = groupRepository.byId(groupId);
        assertTrue(dbGroup.containsMember(member2.getUserId()));
        assertFalse(dbGroup.containsMember(member1.getUserId()));
    }

    @Test
    void remove_member_should_also_remove_managers() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member1.getUserId(), member2.getUserId());
        GroupApi.addGroupManager(manager.getJwt(), groupId, member1.getUserId());

        GroupMembersChangedEvent evt = latestEventFor(groupId, GROUP_MEMBERS_CHANGED, GroupMembersChangedEvent.class);
        assertEquals(groupId, evt.getGroupId());

        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(member1.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));

        // When
        GroupApi.removeGroupMember(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group updatedGroup = groupRepository.byId(groupId);
        assertTrue(updatedGroup.containsMember(member2.getUserId()));
        assertFalse(updatedGroup.containsManager(member1.getUserId()));
        assertFalse(updatedGroup.containsMember(member1.getUserId()));

        GroupMembersChangedEvent anotherEvt = latestEventFor(groupId, GROUP_MEMBERS_CHANGED, GroupMembersChangedEvent.class);
        assertEquals(groupId, anotherEvt.getGroupId());
        assertNotEquals(evt.getId(), anotherEvt.getId());
    }

    @Test
    void should_add_group_manager() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member1.getUserId(), member2.getUserId());

        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(manager.getUserId()));
        assertEquals(1, group.allManagerIds().size());
        assertTrue(group.containsMember(member1.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));

        // When
        GroupApi.addGroupManager(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group updatedGroup = groupRepository.byId(groupId);
        assertTrue(updatedGroup.containsManager(member1.getUserId()));
        assertEquals(2, updatedGroup.allManagerIds().size());

        GroupMembersChangedEvent evt = latestEventFor(groupId, GROUP_MEMBERS_CHANGED, GroupMembersChangedEvent.class);
        assertEquals(groupId, evt.getGroupId());
    }

    @Test
    void add_group_manager_should_also_add_as_member() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        // When
        GroupApi.addGroupManager(manager.getJwt(), groupId, member.getUserId());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(member.getUserId()));
        assertTrue(group.containsMember(member.getUserId()));
    }

    @Test
    void should_add_group_managers() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        // When
        GroupApi.addGroupManagers(manager.getJwt(), groupId, AddGroupManagersCommand.builder()
                .managerIds(List.of(member1.getUserId(), member2.getUserId()))
                .build());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(member1.getUserId()));
        assertTrue(group.containsMember(member1.getUserId()));
        assertTrue(group.containsManager(member2.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));

        GroupMembersChangedEvent evt = latestEventFor(groupId, GROUP_MEMBERS_CHANGED, GroupMembersChangedEvent.class);
        assertEquals(groupId, evt.getGroupId());
    }

    @Test
    void should_remove_group_manager() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        LoginResponse member1 = setupApi.registerWithLogin();
        LoginResponse member2 = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, member1.getUserId(), member2.getUserId());
        GroupApi.addGroupManager(manager.getJwt(), groupId, member1.getUserId());

        // When
        GroupApi.removeGroupManager(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsManager(manager.getUserId()));
        assertFalse(group.containsManager(member1.getUserId()));
    }

    @Test
    void should_delete_group() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        GroupApi.createGroup(manager.getJwt());
        String groupId = GroupApi.createGroup(manager.getJwt());
        Group group = groupRepository.byId(groupId);

        // When
        GroupApi.deleteGroup(manager.getJwt(), groupId);

        // Then
        assertFalse(groupRepository.byIdOptional(groupId).isPresent());

        GroupDeletedEvent evt = latestEventFor(groupId, GROUP_DELETED, GroupDeletedEvent.class);
        assertEquals(group.getCustomId(), evt.getCustomId());

        awaitEventConsumed(evt); // TODO 这个事件为什么执行得这么慢
        assertFalse(folderRepository.existsRoot(group.getCustomId()));
    }

    @Test
    void should_fail_delete_group_if_only_one_visible_group_left() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());

        // When & Then
        assertError(() -> GroupApi.deleteGroupRaw(manager.getJwt(), groupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    void should_deactivate_and_activate_group() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        GroupApi.createGroup(manager.getJwt());

        String groupId = GroupApi.createGroup(manager.getJwt());
        assertTrue(groupRepository.byId(groupId).isActive());

        // When
        GroupApi.deactivateGroup(manager.getJwt(), groupId);

        // Then
        assertFalse(groupRepository.byId(groupId).isActive());

        // When
        GroupApi.activateGroup(manager.getJwt(), groupId);

        // Then
        assertTrue(groupRepository.byId(groupId).isActive());
    }

    @Test
    void should_not_deactivate_if_only_one_active_group_left() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());
        String anotherGroupId = GroupApi.createGroup(manager.getJwt());

        GroupApi.deactivateGroup(manager.getJwt(), anotherGroupId);

        // When & Then
        assertError(() -> GroupApi.deactivateGroupRaw(manager.getJwt(), groupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    void should_add_grant() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        String customId = personalCustomId(manager.getUserId());
        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        Set<Permission> permissions = Set.of(READ, WRITE);

        // When
        GroupApi.addGrant(manager.getJwt(), AddGrantCommand.builder()
                .groupId(groupId)
                .folderId(folderId)
                .permissions(permissions)
                .build());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsFolder(folderId));

        Set<Permission> dbPermissions = group.permissionsOf(List.of(folderId));
        assertEquals(permissions, dbPermissions);
    }

    @Test
    void should_fail_to_add_grant_if_folder_not_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());
        Set<Permission> permissions = Set.of(READ, WRITE);

        // When & Then
        assertError(() -> GroupApi.addGrantRaw(manager.getJwt(), AddGrantCommand.builder()
                .groupId(groupId)
                .folderId(Folder.newFolderId())
                .permissions(permissions)
                .build()), NOT_ALL_FOLDERS_EXIST);
    }

    @Test
    void should_add_grants() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());

        String customId = personalCustomId(manager.getUserId());
        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String folderId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        Set<Permission> permissions = Set.of(READ, WRITE);

        // When
        GroupApi.addGrants(manager.getJwt(), AddGrantsCommand.builder()
                .groupId(groupId)
                .folderIds(List.of(folderId1, folderId2))
                .permissions(permissions)
                .build());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsFolder(folderId1));
        assertTrue(group.containsFolder(folderId2));

        Set<Permission> dbPermissions1 = group.permissionsOf(List.of(folderId1));
        assertEquals(permissions, dbPermissions1);

        Set<Permission> dbPermissions2 = group.permissionsOf(List.of(folderId2));
        assertEquals(permissions, dbPermissions2);
    }

    @Test
    void should_fail_to_add_grants_if_folder_not_all_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String groupId = GroupApi.createGroup(manager.getJwt());

        String customId = personalCustomId(manager.getUserId());
        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        Set<Permission> permissions = Set.of(READ, WRITE);

        // When & Then
        assertError(() -> GroupApi.addGrantsRaw(manager.getJwt(), AddGrantsCommand.builder()
                .groupId(groupId)
                .folderIds(List.of(folderId, Folder.newFolderId()))
                .permissions(permissions)
                .build()), NOT_ALL_FOLDERS_EXIST);
    }

    @Test
    void should_fetch_group_folders() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();

        String groupId = GroupApi.createGroup(manager.getJwt());
        String customId = personalCustomId(manager.getUserId());
        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        GroupApi.addGrant(manager.getJwt(), groupId, folderId);

        // When
        GroupFoldersResponse response = GroupApi.fetchGroupFolders(manager.getJwt(), groupId);

        // Then
        var groupFolders = response.getGroupFolders();
        assertEquals(1, groupFolders.size());
        assertEquals(folderId, groupFolders.get(0).getFolderId());
    }

    @Test
    void should_fetch_group_ordinary_members() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String userId1 = setupApi.registerWithLogin().getUserId();
        String userId2 = setupApi.registerWithLogin().getUserId();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupMembers(manager.getJwt(), groupId, userId1);
        GroupApi.addGroupManager(manager.getJwt(), groupId, userId2);

        // When
        var response = GroupApi.fetchGroupOrdinaryMembers(manager.getJwt(), groupId);

        // Then
        var groupOrdinaryMembers = response.getGroupOrdinaryMembers();
        assertEquals(1, groupOrdinaryMembers.size());

        User user = userRepository.byId(userId1);
        assertEquals(user.getUsername(), groupOrdinaryMembers.get(0).getUsername());
    }

    @Test
    void should_fetch_group_managers() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String userId = setupApi.registerWithLogin().getUserId();

        String groupId = GroupApi.createGroup(manager.getJwt());
        GroupApi.addGroupManager(manager.getJwt(), groupId, userId);

        // When
        var response = GroupApi.fetchGroupManagers(manager.getJwt(), groupId);

        // Then
        var groupManagers = response.getGroupManagers();
        assertEquals(2, groupManagers.size());
    }

    @Test
    void should_page_my_groups_as_for_manager() {
        // Given
        LoginResponse manager1 = setupApi.registerWithLogin();
        LoginResponse manager2 = setupApi.registerWithLogin();

        String groupNamePrefix = "Group";
        String groupId1 = GroupApi.createGroup(manager1.getJwt(), groupNamePrefix + "1");
        String groupId2 = GroupApi.createGroup(manager2.getJwt(), groupNamePrefix + "2");
        GroupApi.addGroupManager(manager2.getJwt(), groupId2, manager1.getUserId());

        // When
        PagedList<GroupResponse> pagedList = GroupApi.pageMyGroupsAsForManager(manager1.getJwt(), MyGroupsAsForManagerPageQuery.builder()
                .search(groupNamePrefix)
                .ascSort(false)
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertEquals(2, pagedList.size());
        assertTrue(pagedList.getData().stream().allMatch(item -> isNotBlank(item.getGroupId())));
    }

    @Test
    void should_page_my_groups_as_for_member() {
        // Given
        LoginResponse manager1 = setupApi.registerWithLogin();
        LoginResponse user1 = setupApi.registerWithLogin();
        LoginResponse user2 = setupApi.registerWithLogin();

        String groupId1 = GroupApi.createGroup(manager1.getJwt());
        String groupId2 = GroupApi.createGroup(user1.getJwt());
        String groupId3 = GroupApi.createGroup(user2.getJwt());

        GroupApi.addGroupMembers(user1.getJwt(), groupId2, manager1.getUserId());
        GroupApi.addGroupManager(user2.getJwt(), groupId3, manager1.getUserId());

        // When
        PagedList<GroupResponse> pagedList = GroupApi.pageMyGroupsAsForMember(manager1.getJwt(), MyGroupsAsForMemberPageQuery.builder()
                .ascSort(false)
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertEquals(3, pagedList.size());
        assertTrue(pagedList.getData().stream().allMatch(item -> isNotBlank(item.getGroupId())));
    }

}
