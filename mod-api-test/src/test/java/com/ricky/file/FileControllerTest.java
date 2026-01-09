package com.ricky.file;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.domain.File;
import com.ricky.file.query.FetchFilePathResponse;
import com.ricky.folder.FolderApi;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.upload.FileUploadApi;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.ricky.RandomTestFixture.rFilename;
import static com.ricky.RandomTestFixture.rFolderName;
import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileControllerTest extends BaseApiTest {

    @Test
    void should_rename_file() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();

        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(loginResponse.getJwt(), file, parentId).getFileId();

        String newName = rFilename();

        // When
        FileApi.renameFile(loginResponse.getJwt(), fileId, RenameFileCommand.builder()
                .newName(newName)
                .build());

        // Then
        File dbFile = fileRepository.byId(fileId);
        assertEquals(newName, dbFile.getFilename());
    }

    @Test
    void should_delete_file_force() throws IOException, InterruptedException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String parentId = FolderApi.createFolder(loginResponse.getJwt(), customId, rFolderName());

        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(loginResponse.getJwt(), file, parentId).getFileId();
        File dbFile = fileRepository.byId(fileId);

        // 这里必须sleep，直接删除会导致文件上传事件还未处理完，导致mongodb写冲突
        Thread.sleep(5 * 1000);

        // When
        FileApi.deleteFileForce(loginResponse.getJwt(), fileId);

        // Then
        assertFalse(fileRepository.exists(dbFile.getId()));
        assertFalse(storageService.exists(dbFile.getStorageId()));
        assertFalse(fileExtraRepository.existsByFileId(dbFile.getId()));
    }

    @Test
    void should_fetch_file_path() throws IOException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String customId = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId()).getCustomId();

        String parentFolderName = rFolderName();
        String childFolderName = rFolderName();
        String parentFolderId = FolderApi.createFolder(loginResponse.getJwt(), customId, parentFolderName);
        String childFolderId = FolderApi.createFolderWithParent(loginResponse.getJwt(), customId, childFolderName, parentFolderId);

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String fileId = FileUploadApi.upload(loginResponse.getJwt(), file, childFolderId).getFileId();

        FolderHierarchy personalSpace = folderHierarchyDomainService.personalSpaceOf(loginResponse.getUserId());

        // When
        FetchFilePathResponse response = FileApi.fetchFilePath(loginResponse.getJwt(), personalSpace.getCustomId(), fileId);

        // Then
        assertEquals(
                parentFolderName + NODE_ID_SEPARATOR + childFolderName + NODE_ID_SEPARATOR + file.getName(),
                response.getPath()
        );
    }
}
