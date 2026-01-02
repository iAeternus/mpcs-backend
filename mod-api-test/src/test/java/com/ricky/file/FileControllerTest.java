package com.ricky.file;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.dto.resp.FetchFilePathResponse;
import com.ricky.folder.FolderApi;
import com.ricky.upload.FileUploadApi;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.ricky.RandomTestFixture.rFolderName;
import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileControllerTest extends BaseApiTest {

    @Test
    void should_fetch_file_path() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String parentFolderName = rFolderName();
        String childFolderName = rFolderName();
        String parentFolderId = FolderApi.createFolder(loginResponse.getJwt(), parentFolderName);
        String childFolderId = FolderApi.createFolderWithParent(loginResponse.getJwt(), childFolderName, parentFolderId);

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String fileId = FileUploadApi.upload(loginResponse.getJwt(), file, childFolderId).getFileId();

        // When
        FetchFilePathResponse response = FileApi.fetchFilePath(loginResponse.getJwt(), fileId);

        // Then
        assertEquals(
                parentFolderName + NODE_ID_SEPARATOR + childFolderName + NODE_ID_SEPARATOR + file.getName(),
                response.getPath()
        );
    }
}
