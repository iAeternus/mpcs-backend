package com.ricky.upload;


import com.ricky.BaseApiTest;
import com.ricky.file.domain.MimeType;
import com.ricky.upload.domain.dto.resp.FileUploadResponse;
import com.ricky.upload.domain.dto.cmd.CompleteUploadCommand;
import com.ricky.upload.domain.dto.cmd.InitUploadCommand;
import com.ricky.upload.domain.dto.resp.InitUploadResponse;
import com.ricky.upload.domain.dto.resp.UploadChunkResponse;
import io.restassured.response.Response;

import java.io.ByteArrayInputStream;
import java.io.File;

public class FileUploadApi {

    private static final String ROOT_URL = "/files/upload";

    public static Response uploadRaw(String token, File file, String parentId) {
        return BaseApiTest.given(token)
                .contentType("multipart/form-data")
                .multiPart("file", file)
                .formParam("parentId", parentId)
                .when()
                .post(ROOT_URL);
    }

    public static FileUploadResponse upload(String token, File file, String parentId) {
        return uploadRaw(token, file, parentId)
                .then()
                .statusCode(201)
                .extract()
                .as(FileUploadResponse.class);
    }

    public static Response initUploadRaw(String token, InitUploadCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL + "/init");
    }

    public static InitUploadResponse initUpload(String token, InitUploadCommand command) {
        return initUploadRaw(token, command)
                .then()
                .statusCode(200)
                .extract()
                .as(InitUploadResponse.class);
    }

    public static Response uploadChunkRaw(String token, String uploadId, Integer chunkIndex, byte[] chunkBytes) {
        return BaseApiTest.given(token)
                .contentType("multipart/form-data")
                .formParam("uploadId", uploadId)
                .formParam("chunkIndex", chunkIndex)
                .multiPart(
                        "chunk",
                        "chunk-" + chunkIndex,
                        new ByteArrayInputStream(chunkBytes),
                        MimeType.IMAGE_PNG.getContentType()
                )
                .when()
                .post(ROOT_URL + "/chunk");
    }

    public static UploadChunkResponse uploadChunk(String token, String uploadId, Integer chunkIndex, byte[] chunkBytes) {
        return uploadChunkRaw(token, uploadId, chunkIndex, chunkBytes)
                .then()
                .statusCode(200)
                .extract()
                .as(UploadChunkResponse.class);
    }

    public static Response completeUploadRaw(String token, CompleteUploadCommand command) {
        return BaseApiTest.given(token)
                .body(command)
                .when()
                .post(ROOT_URL + "/complete");
    }

    public static FileUploadResponse completeUpload(String token, CompleteUploadCommand command) {
        return completeUploadRaw(token, command)
                .then()
                .statusCode(201)
                .extract()
                .as(FileUploadResponse.class);
    }

}
