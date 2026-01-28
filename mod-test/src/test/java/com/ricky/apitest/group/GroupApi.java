package com.ricky.apitest.group;

import com.ricky.apitest.BaseApiTest;
import com.ricky.common.auth.Permission;
import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.page.PagedList;
import com.ricky.group.command.*;
import com.ricky.group.query.*;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;
import java.util.Set;

import static com.ricky.apitest.RandomTestFixture.*;

public class GroupApi {

    private static final String ROOT_URL = "/groups";

    public static Response createGroupRaw(String jwt, CreateGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post(ROOT_URL);
    }

    public static String createGroup(String jwt, CreateGroupCommand command) {
        return createGroupRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }

    public static String createGroup(String jwt, String groupName) {
        return createGroup(jwt, CreateGroupCommand.builder().name(groupName).customId(rCustomId()).build());
    }

    public static String createGroup(String jwt) {
        return createGroup(jwt, CreateGroupCommand.builder().name(rGroupName()).customId(rCustomId()).build());
    }

    public static Response renameGroupRaw(String jwt, String groupId, RenameGroupCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put(ROOT_URL + "/{groupId}/name", groupId);
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
                .put(ROOT_URL + "/{groupId}/members", groupId);
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
                .delete(ROOT_URL + "/{groupId}/members/{memberId}", groupId, memberId);
    }

    public static void removeGroupMember(String jwt, String groupId, String memberId) {
        removeGroupMemberRaw(jwt, groupId, memberId)
                .then()
                .statusCode(200);
    }

    public static Response addGroupManagerRaw(String jwt, String groupId, String memberId) {
        return BaseApiTest.given(jwt)
                .when()
                .put(ROOT_URL + "/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void addGroupManager(String jwt, String groupId, String memberId) {
        addGroupManagerRaw(jwt, groupId, memberId).then().statusCode(200);
    }

    public static Response addGroupManagersRaw(String jwt, String groupId, AddGroupManagersCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .put(ROOT_URL + "/{groupId}/managers", groupId);
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
                .delete(ROOT_URL + "/{groupId}/managers/{memberId}", groupId, memberId);
    }

    public static void removeGroupManager(String jwt, String groupId, String memberId) {
        removeGroupManagerRaw(jwt, groupId, memberId)
                .then()
                .statusCode(200);
    }

    public static Response deleteGroupRaw(String jwt, String groupId) {
        return BaseApiTest.given(jwt)
                .when()
                .delete(ROOT_URL + "/{groupId}", groupId);
    }

    public static void deleteGroup(String jwt, String groupId) {
        deleteGroupRaw(jwt, groupId)
                .then()
                .statusCode(200);
    }

    public static void activateGroup(String jwt, String groupId) {
        BaseApiTest.given(jwt)
                .when()
                .put(ROOT_URL + "/{groupId}/activation", groupId)
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
                .put(ROOT_URL + "/{groupId}/deactivation", groupId);
    }

    public static Response addGrantRaw(String token, AddGrantCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/grant");
    }

    public static void addGrant(String token, AddGrantCommand command) {
        addGrantRaw(token, command)
                .then()
                .statusCode(200);
    }

    public static void addGrant(String token, String groupId, String folderId) {
        addGrant(token, AddGrantCommand.builder()
                .groupId(groupId)
                .folderId(folderId)
                .permissions(Set.of(rEnumOf(Permission.class)))
                .build());
    }

    public static Response addGrantsRaw(String token, AddGrantsCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/grants");
    }

    public static void addGrants(String token, AddGrantsCommand command) {
        addGrantsRaw(token, command)
                .then()
                .statusCode(200);
    }

    public static GroupFoldersResponse fetchGroupFolders(String token, String groupId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{groupId}/folders", groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(GroupFoldersResponse.class);
    }

    public static GroupOrdinaryMembersResponse fetchGroupOrdinaryMembers(String token, String groupId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{groupId}/ordinary-member", groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(GroupOrdinaryMembersResponse.class);
    }

    public static GroupManagersResponse fetchGroupManagers(String token, String groupId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{groupId}/managers", groupId)
                .then()
                .statusCode(200)
                .extract()
                .as(GroupManagersResponse.class);
    }

    public static PagedList<GroupResponse> pageMyGroupsAsForManager(String token, MyGroupsAsForManaberPageQuery query) {
        return BaseApiTest.given(token)
                .body(query)
                .when()
                .post(ROOT_URL + "/page/my-groups")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static PagedList<GroupResponse> pageMyGroupsAsForMember(String token, MyGroupsAsForMemberPageQuery query) {
        return BaseApiTest.given(token)
                .body(query)
                .when()
                .post(ROOT_URL + "/page/my-joined")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
