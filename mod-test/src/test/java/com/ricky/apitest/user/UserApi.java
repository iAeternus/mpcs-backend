package com.ricky.apitest.user;

import com.ricky.apitest.BaseApiTest;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import io.restassured.response.Response;

public class UserApi {

    private static final String ROOT_URL = "/user";

    public static RegisterResponse register(RegisterCommand command) {
        return registerRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(RegisterResponse.class);
    }

    public static Response registerRaw(RegisterCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post(ROOT_URL + "/registration");
    }

    public static UserInfoResponse myUserInfo(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get(ROOT_URL + "/me/info")
                .then()
                .statusCode(200)
                .extract()
                .as(UserInfoResponse.class);
    }

    public static UserProfileResponse myProfile(String jwt) {
        return myProfileRaw(jwt)
                .then()
                .statusCode(200)
                .extract()
                .as(UserProfileResponse.class);
    }

    public static Response myProfileRaw(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get(ROOT_URL + "/me");
    }
}
