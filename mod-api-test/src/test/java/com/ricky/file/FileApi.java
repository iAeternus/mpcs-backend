package com.ricky.file;


import com.ricky.common.utils.MyObjectMapper;
import com.ricky.file.domain.dto.FileUploadCommand;
import com.ricky.testsuite.ApiTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

@Component
public class FileApi {

    private static final String ROOT_URL = "/files";

    @Autowired
    private MyObjectMapper objectMapper;

    public ApiTest.ResponseExecutor uploadRaw(MockMvc mockMvc, String token, FileUploadCommand command) {
        return new ApiTest(mockMvc, objectMapper)
                .post(ROOT_URL)
                .bearerToken(token)
                .file(command.getFile())
                .param("parentId", command.getParentId())
                .param("path", command.getPath())
                .execute();
    }

    public String upload(MockMvc mockMvc, String token, FileUploadCommand command) {
        return uploadRaw(mockMvc, token, command)
                .expectStatus(200)
                .as(String.class);
    }

}
