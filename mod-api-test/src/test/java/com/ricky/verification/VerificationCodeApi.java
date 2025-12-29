package com.ricky.verification;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.verification.domain.dto.cmd.*;
import io.restassured.response.Response;

public class VerificationCodeApi {

    public static Response createVerificationCodeForRegisterRaw(CreateRegisterVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-register");
    }

    public static String createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand command) {
        return createVerificationCodeForRegisterRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }

    public static String createVerificationCodeForRegister(String mobileOrEmail) {
        return createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobileOrEmail).build());
    }

    public static Response createVerificationCodeForLoginRaw(CreateLoginVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-login");
    }

    public static String createVerificationCodeForLogin(CreateLoginVerificationCodeCommand command) {
        return createVerificationCodeForLoginRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }


    public static Response createVerificationCodeForFindBackPasswordRaw(CreateFindBackPasswordVerificationCodeCommand command) {
        return BaseApiTest.given()
                .body(command)
                .when()
                .post("/verification-codes/for-find-back-password");
    }

    public static String createVerificationCodeForFindBackPassword(CreateFindBackPasswordVerificationCodeCommand command) {
        return createVerificationCodeForFindBackPasswordRaw(command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }

    public static Response createVerificationCodeForChangeMobileRaw(String jwt, CreateChangeMobileVerificationCodeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/verification-codes/for-change-mobile");
    }

    public static String createVerificationCodeForChangeMobile(String jwt, CreateChangeMobileVerificationCodeCommand command) {
        return createVerificationCodeForChangeMobileRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }

    public static Response createVerificationCodeForIdentifyMobileRaw(String jwt, IdentifyMobileVerificationCodeCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/verification-codes/for-identify-mobile");
    }

    public static String createVerificationCodeForIdentifyMobile(String jwt, IdentifyMobileVerificationCodeCommand command) {
        return createVerificationCodeForIdentifyMobileRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(IdResponse.class).toString();
    }
}
