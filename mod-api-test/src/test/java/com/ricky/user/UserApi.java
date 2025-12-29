package com.ricky.user;

import com.ricky.BaseApiTest;
import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
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

}
