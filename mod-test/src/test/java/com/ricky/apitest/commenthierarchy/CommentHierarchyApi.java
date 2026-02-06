//package com.ricky.apitest.commenthierarchy;
//
//import com.ricky.apitest.BaseApiTest;
//import com.ricky.commenthierarchy.command.ReplyCommand;
//import com.ricky.commenthierarchy.command.ReplyResponse;
//import io.restassured.response.Response;
//
//public class CommentHierarchyApi {
//
//    private static final String ROOT_URL = "/replies";
//
//    public static Response replyRaw(String token, ReplyCommand command) {
//        return BaseApiTest.given(token)
//                .body(command)
//                .when()
//                .post(ROOT_URL);
//    }
//
//    public static ReplyResponse reply(String token, ReplyCommand command) {
//        return replyRaw(token, command)
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(ReplyResponse.class);
//    }
//
//}
