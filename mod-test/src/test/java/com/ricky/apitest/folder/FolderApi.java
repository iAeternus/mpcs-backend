package com.ricky.apitest.folder;

import com.ricky.apitest.BaseApiTest;
import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.folder.command.*;
import com.ricky.folder.query.FolderContentResponse;
import io.restassured.response.Response;

public class FolderApi {

    private static final String ROOT_URL = "/folders";

    public static Response createFolderRaw(String token, CreateFolderCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL);
    }

    public static String createFolder(String token, CreateFolderCommand command) {
        return createFolderRaw(token, command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).getId();
    }

    public static String createFolder(String token, String customId, String folderName) {
        return createFolderRaw(token, CreateFolderCommand.builder().customId(customId).folderName(folderName).build())
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).getId();
    }

    public static String createFolderWithParent(String token, String customId, String folderName, String parentId) {
        return createFolderRaw(token, CreateFolderCommand.builder().customId(customId).folderName(folderName).parentId(parentId).build())
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).getId();
    }

    public static Response renameFolderRaw(String token, String folderId, RenameFolderCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL + "/{folderId}/name", folderId);
    }

    public static void renameFolder(String token, String folderId, RenameFolderCommand command) {
        renameFolderRaw(token, folderId, command)
                .then()
                .statusCode(200);
    }

    public static Response deleteFolderForceRaw(String token, String folderId, DeleteFolderForceCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .delete(ROOT_URL + "/{folderId}/delete-force", folderId);
    }

    public static void deleteFolderForce(String token, String folderId, DeleteFolderForceCommand command) {
        deleteFolderForceRaw(token, folderId, command)
                .then()
                .statusCode(200);
    }

    public static Response moveFolderRaw(String token, MoveFolderCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/move");
    }

    public static MoveFolderResponse moveFolder(String token, MoveFolderCommand command) {
        return moveFolderRaw(token, command)
                .then()
                .statusCode(200)
                .extract()
                .as(MoveFolderResponse.class);
    }

    public static FolderContentResponse fetchFolderContent(String token, String customId, String folderId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{customId}/{folderId}", customId, folderId)
                .then()
                .statusCode(200)
                .extract()
                .as(FolderContentResponse.class);
    }

}
