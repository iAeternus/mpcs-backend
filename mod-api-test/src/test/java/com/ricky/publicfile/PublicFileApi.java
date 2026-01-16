package com.ricky.publicfile;

import com.ricky.BaseApiTest;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.command.PostResponse;
import io.restassured.response.Response;

public class PublicFileApi {

    private static final String ROOT_URL = "/public-files";

    public static Response postRaw(String token, PostCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL);
    }

    public static PostResponse post(String token, PostCommand command) {
        return postRaw(token, command)
                .then()
                .statusCode(200)
                .extract()
                .as(PostResponse.class);
    }

    public static PostResponse post(String token, String fileId) {
        return PublicFileApi.post(token, PostCommand.builder()
                .fileId(fileId)
                .build());
    }

    public static Response withdrawRaw(String token, String postId) {
        return BaseApiTest.given(token)
                .when()
                .delete(ROOT_URL + "/{postId}/withdraw", postId);
    }

    public static void withdraw(String token, String postId) {
        withdrawRaw(token, postId)
                .then()
                .statusCode(200);
    }

    public static void updateTitle(String token, ModifyTitleCommand command) {
        BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/title")
                .then()
                .statusCode(200);
    }

    public static void editDescription(String token, EditDescriptionCommand command) {
        BaseApiTest.given(token)
                .body(command)
                .when()
                .put(ROOT_URL + "/description")
                .then()
                .statusCode(200);
    }

}