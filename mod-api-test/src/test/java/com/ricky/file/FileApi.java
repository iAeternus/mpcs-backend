package com.ricky.file;

import com.ricky.BaseApiTest;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.query.FetchFilePathResponse;
import io.restassured.response.Response;

public class FileApi {

    private static final String ROOT_URL = "/files";

    public static Response renameFileRaw(String token, String fileId, RenameFileCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/{fileId}/name", fileId);
    }

    public static void renameFile(String token, String fileId, RenameFileCommand command) {
        renameFileRaw(token, fileId, command)
                .then()
                .statusCode(200);
    }

    public static Response deleteFileForceRaw(String token, String fileId) {
        return BaseApiTest.given(token)
                .when()
                .delete(ROOT_URL + "/{fileId}/delete-force", fileId);
    }

    public static void deleteFileForce(String token, String fileId) {
        deleteFileForceRaw(token, fileId)
                .then()
                .statusCode(200);
    }

    public static FetchFilePathResponse fetchFilePath(String token, String fileId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{fileId}/path", fileId)
                .then()
                .statusCode(200)
                .extract()
                .as(FetchFilePathResponse.class);
    }

}
