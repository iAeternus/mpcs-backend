package com.ricky.apitest.folderhierarchy;

import com.ricky.apitest.BaseApiTest;
import com.ricky.folderhierarchy.command.UpdateFolderHierarchyCommand;
import com.ricky.folderhierarchy.query.FolderHierarchyResponse;
import io.restassured.response.Response;

public class FolderHierarchyApi {

    private static final String ROOT_URL = "/folder-hierarchy";

    public static Response updateFolderHierarchyRaw(String token, UpdateFolderHierarchyCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .put(ROOT_URL);
    }

    public static void updateFolderHierarchy(String token, UpdateFolderHierarchyCommand command) {
        updateFolderHierarchyRaw(token, command)
                .then()
                .statusCode(200);
    }

    public static Response fetchFolderHierarchyRaw(String token, String customId) {
        return BaseApiTest.given(token)
                .get(ROOT_URL + "/{customId}", customId);
    }

    public static FolderHierarchyResponse fetchFolderHierarchy(String token, String customId) {
        return fetchFolderHierarchyRaw(token, customId)
                .then()
                .statusCode(200)
                .extract()
                .as(FolderHierarchyResponse.class);
    }

}
