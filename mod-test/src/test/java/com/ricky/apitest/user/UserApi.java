package com.ricky.apitest.user;

import com.ricky.apitest.BaseApiTest;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.command.UploadAvatarResponse;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import io.restassured.response.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    public static Response uploadAvatarRaw(String jwt, File avatar) {
        return BaseApiTest.given(jwt)
                .contentType("multipart/form-data")
                .multiPart("avatar", avatar)
                .when()
                .post(ROOT_URL + "/me/avatar");
    }

    public static Response uploadAvatarRaw(String jwt, String filename, byte[] content, String contentType) {
        return BaseApiTest.given(jwt)
                .contentType("multipart/form-data")
                .multiPart("avatar", filename, content, contentType)
                .when()
                .post(ROOT_URL + "/me/avatar");
    }

    public static UploadAvatarResponse uploadAvatar(String jwt, File avatar) {
        try {
            return uploadAvatar(jwt, avatar.getName(), Files.readAllBytes(avatar.toPath()), avatarContentType(avatar.getName()));
        } catch (IOException e) {
            throw new IllegalStateException("Read avatar file failed", e);
        }
    }

    public static UploadAvatarResponse uploadAvatar(String jwt, String filename, byte[] content, String contentType) {
        return uploadAvatarRaw(jwt, filename, content, contentType)
                .then()
                .statusCode(201)
                .extract()
                .as(UploadAvatarResponse.class);
    }

    private static String avatarContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "application/octet-stream";
    }

}
