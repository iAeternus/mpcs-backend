package com.ricky.apitest.publicfile;

import com.ricky.apitest.BaseApiTest;
import com.ricky.common.domain.page.PagedList;
import com.ricky.publicfile.command.EditDescriptionCommand;
import com.ricky.publicfile.command.ModifyTitleCommand;
import com.ricky.publicfile.command.PostCommand;
import com.ricky.publicfile.command.PostResponse;
import com.ricky.publicfile.query.CommentCountResponse;
import com.ricky.publicfile.query.LikeCountResponse;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.Map;

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
        return post(token, PostCommand.builder()
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

    public static PagedList<PublicFileResponse> page(String token, PublicFilePageQuery query) {
        return BaseApiTest.given(token)
                .body(query)
                .when()
                .post(ROOT_URL + "/page")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static CommentCountResponse fetchCommentCount(String token, String postId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{postId}/comment", postId)
                .then()
                .statusCode(200)
                .extract()
                .as(CommentCountResponse.class);
    }

    public static LikeCountResponse fetchLikeCount(String token, String postId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{postId}/like", postId)
                .then()
                .statusCode(200)
                .extract()
                .as(LikeCountResponse.class);
    }

}