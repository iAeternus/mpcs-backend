package com.ricky.group;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.group.command.AddGroupManagersCommand;
import com.ricky.group.command.AddGroupMembersCommand;
import com.ricky.group.command.CreateGroupCommand;
import com.ricky.group.command.RenameGroupCommand;
import io.restassured.response.Response;

import java.util.List;

import static com.ricky.RandomTestFixture.rGroupName;

public class GroupApi {

    public static Response createGroupRaw(String jwt, CreateGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/groups");
    }

    public static String createGroup(String jwt, CreateGroupCommand command) {
        return createGroupRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }

    public static String createGroup(String jwt, String groupName) {
        return createGroup(jwt, CreateGroupCommand.builder().name(groupName).build());
    }

    public static String createGroup(String jwt) {
        return createGroup(jwt, CreateGroupCommand.builder().name(rGroupName()).build());
    }

    public static Response renameGroupRaw(String jwt, String groupId, RenameGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/name", groupId);
    }

    public static void renameGroup(String jwt, String groupId, RenameGroupCommand command) {
        renameGroupRaw(jwt, groupId, command)
                .then()
                .statusCode(200);
    }

    public static void renameGroup(String jwt, String groupId, String name) {
        renameGroup(jwt, groupId, RenameGroupCommand.builder().newName(name).build());
    }

    public static Response addGroupMembersRaw(String jwt, String groupId, AddGroupMembersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/members", groupId);
    }

    public static void addGroupMembers(String jwt, String groupId, AddGroupMembersCommand command) {
        addGroupMembersRaw(jwt, groupId, command)
                .then()
                .statusCode(200);
    }

    public static void addGroupMembers(String jwt, String groupId, String... memberIds) {
        addGroupMembersRaw(jwt, groupId, AddGroupMembersCommand.builder().memberIds(List.of(memberIds)).build())
                .then()
                .statusCode(200);
    }

    public static Response removeGroupMemberRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{groupId}/members/{memberId}", groupId, memberId);
    }

    public static void removeGroupMember(String jwt, String groupId, String memberId) {
        removeGroupMemberRaw(jwt, groupId, memberId)
                .then()
                .statusCode(200);
    }

    public static Response addGroupManagerRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void addGroupManager(String jwt, String groupId, String memberId) {
        addGroupManagerRaw(jwt, groupId, memberId).then().statusCode(200);
    }

    public static Response addGroupManagersRaw(String jwt, String groupId, AddGroupManagersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put("/groups/{groupId}/managers", groupId);
    }

    public static void addGroupManagers(String jwt, String groupId, AddGroupManagersCommand command) {
        addGroupManagersRaw(jwt, groupId, command)
                .then()
                .statusCode(200);
    }

    public static void addGroupManagers(String jwt, String groupId, String... managerIds) {
        addGroupManagers(jwt, groupId, AddGroupManagersCommand.builder()
                .managerIds(List.of(managerIds))
                .build());
    }

    public static Response removeGroupManagerRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void removeGroupManager(String jwt, String groupId, String memberId) {
        removeGroupManagerRaw(jwt, groupId, memberId)
                .then()
                .statusCode(200);
    }

    public static Response deleteGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete("/groups/{id}", groupId);
    }

    public static void deleteGroup(String jwt, String groupId) {
        deleteGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static void activateGroup(String jwt, String groupId) {
        BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/activation", groupId)
                .then()
                .statusCode(200);
    }

    public static void deactivateGroup(String jwt, String groupId) {
        deactivateGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static Response deactivateGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .put("/groups/{groupId}/deactivation", groupId);
    }

}
