package com.ricky.file;

import com.ricky.file.domain.dto.FileUploadCommand;
import com.ricky.folder.domain.Folder;
import com.ricky.testsuite.BaseApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
class FileControllerTest extends BaseApiTest {

    @Autowired
    private FileApi fileApi;

    @Test
    void should_upload_file() {
        // Given
        MultipartFile file = setupApi.loadTestFile("test_file.txt", "file");
        FileUploadCommand command = FileUploadCommand.builder()
                .file(file)
                .parentId(Folder.newFolderId())
                .path("/test")
                .build();

        // When
        String fileId = fileApi.upload(mockMvc, "", command);

        // Then
        assertNotNull(fileId); // TODO 完善这里的断言
    }

}