package com.ricky.apitest.comment;

import com.ricky.apitest.BaseApiTest;
import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.query.*;
import com.ricky.common.domain.page.PagedList;
import io.restassured.common.mapper.TypeRef;
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

    public static CreateCommentResponse createReply(String token, String postId, String parentId) {
        return createComment(token, CreateCommentCommand.builder()
                .postId(postId)
                .parentId(parentId)
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

    public static CommentResponse fetchDetail(String token, String commentId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{commentId}", commentId)
                .then()
                .statusCode(200)
                .extract()
                .as(CommentResponse.class);
    }

    public static PagedList<CommentResponse> page(String token, CommentPageQuery query) {
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

    public static PagedList<CommentResponse> pageDirect(String token, DirectReplyPageQuery query) {
        return BaseApiTest.given(token)
                .body(query)
                .when()
                .post(ROOT_URL + "/page/direct")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static PagedList<MyCommentResponse> pageMyComment(String token, MyCommentPageQuery query) {
        return BaseApiTest.given(token)
                .body(query)
                .when()
                .post(ROOT_URL + "/page/my")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

}
