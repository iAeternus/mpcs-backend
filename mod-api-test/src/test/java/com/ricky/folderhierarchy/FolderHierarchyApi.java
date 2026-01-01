package com.ricky.folderhierarchy;

import com.ricky.BaseApiTest;
import com.ricky.folderhierarchy.domain.dto.cmd.UpdateFolderHierarchyCommand;
import com.ricky.folderhierarchy.domain.dto.resp.FolderHierarchyResponse;
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

    public static Response fetchFolderHierarchyRaw(String token) {
        return BaseApiTest.given(token)
                .get(ROOT_URL);
    }

    public static FolderHierarchyResponse fetchFolderHierarchy(String token) {
        return fetchFolderHierarchyRaw(token)
                .then()
                .statusCode(200)
                .extract()
                .as(FolderHierarchyResponse.class);
    }

}
