package com.ricky.apitest.comment;

import com.ricky.apitest.BaseApiTest;
import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import io.restassured.response.Response;

import static com.ricky.apitest.RandomTestFixture.rCommentContent;

public class CommentApi {

    private static final String ROOT_URL = "/comments";

    public static Response createCommentRaw(String token, CreateCommentCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL);
    }

    public static CreateCommentResponse createComment(String token, CreateCommentCommand command) {
        return createCommentRaw(token, command)
                .then()
                .statusCode(200)
                .extract()
                .as(CreateCommentResponse.class);
    }

    public static CreateCommentResponse createComment(String token, String postId) {
        return createComment(token, CreateCommentCommand.builder()
                .postId(postId)
                .content(rCommentContent())
                .build());
    }

    public static Response deleteCommentRaw(String token, DeleteCommentCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .delete(ROOT_URL);
    }

    public static void deleteComment(String token, DeleteCommentCommand command) {
        deleteCommentRaw(token, command)
                .then()
                .statusCode(200);
    }

}
