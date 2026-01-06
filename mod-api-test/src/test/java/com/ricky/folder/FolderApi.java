package com.ricky.folder;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.folder.command.CreateFolderCommand;
import com.ricky.folder.command.RenameFolderCommand;
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

    public static String createFolder(String token, String folderName) {
        return createFolderRaw(token, CreateFolderCommand.builder().folderName(folderName).build())
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).getId();
    }

    public static String createFolderWithParent(String token, String folderName, String parentId) {
        return createFolderRaw(token, CreateFolderCommand.builder().folderName(folderName).parentId(parentId).build())
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

    public static Response deleteFolderForceRaw(String token, String folderId) {
        return BaseApiTest.given(token)
                .when()
                .delete(ROOT_URL + "/{folderId}/delete-force", folderId);
    }

    public static void deleteFolderForce(String token, String folderId) {
        deleteFolderForceRaw(token, folderId)
                .then()
                .statusCode(200);
    }

}
