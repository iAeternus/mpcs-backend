package com.ricky.apitest.group;

import com.ricky.apitest.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.group.command.AddGroupManagersCommand;
import com.ricky.group.command.AddGroupMembersCommand;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.command.RenameGroupCommand;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.event.GroupMembersChangedEvent;
import com.ricky.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ricky.apitest.RandomTestFixture.rGroupName;
import static com.ricky.common.event.DomainEventType.GROUP_MEMBERS_CHANGED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
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
    void should_fail_create_group_if_name_already_exits() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).build();
        GroupApi.createGroup(loginResponse.getJwt(), command);

        // When & Then
        assertError(() -> GroupApi.createGroupRaw(loginResponse.getJwt(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

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
        assertTrue(groupRepository.byId(groupId).getMembers().containsAll(List.of(member1.getUserId(), member2.getUserId())));

        // When
        GroupApi.removeGroupMember(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group group = groupRepository.byId(groupId);
        assertTrue(group.containsMember(member2.getUserId()));
        assertFalse(group.containsMember(member1.getUserId()));
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
        assertEquals(1, group.getManagers().size());
        assertTrue(group.containsMember(member1.getUserId()));
        assertTrue(group.containsMember(member2.getUserId()));

        // When
        GroupApi.addGroupManager(manager.getJwt(), groupId, member1.getUserId());

        // Then
        Group updatedGroup = groupRepository.byId(groupId);
        assertTrue(updatedGroup.containsManager(member1.getUserId()));
        assertEquals(2, updatedGroup.getManagers().size());

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

        // When
        GroupApi.deleteGroup(manager.getJwt(), groupId);

        // Then
        assertFalse(groupRepository.byIdOptional(groupId).isPresent());
    }

    @Test
    public void should_raise_event_when_delete_group() {
        // TODO
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

}
