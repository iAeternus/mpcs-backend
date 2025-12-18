package com.ricky.file;


import com.ricky.BaseApiTest;
import com.ricky.file.domain.dto.resp.FileUploadResponse;
import io.restassured.response.Response;

import java.io.File;

public class FileApi {

    private static final String ROOT_URL = "/files";

    public static Response uploadRaw(String token, File file, String parentId, String path) {
        return BaseApiTest.given(token)
                .contentType("multipart/form-data")
                .multiPart("file", file)
                .multiPart("parentId", parentId)
                .multiPart("path", path)
                .when()
                .post(ROOT_URL);
    }

    public static FileUploadResponse upload(String token, File file, String parentId, String path) {
        return uploadRaw(token, file, parentId, path)
                .then()
                .statusCode(201)
                .extract()
                .as(FileUploadResponse.class);
    }

}
