package com.ricky.file;

import com.ricky.BaseApiTest;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;

public class FileApi {

    private static final String ROOT_URL = "/files";

    public static FetchFilePathResponse fetchFilePath(String token, String fileId) {
        return BaseApiTest.given(token)
                .when()
                .get(ROOT_URL + "/{fileId}/path", fileId)
                .then()
                .statusCode(200)
                .extract()
                .as(FetchFilePathResponse.class);
    }

}
