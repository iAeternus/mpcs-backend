package com.ricky.apitest.file;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.TestFileContext;
import com.ricky.apitest.folder.FolderApi;
import com.ricky.apitest.upload.FileUploadApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.command.MoveFileCommand;
import com.ricky.file.command.RenameFileCommand;
import com.ricky.file.domain.File;
import com.ricky.file.query.FileInfoResponse;
import com.ricky.file.query.FilePathResponse;
import com.ricky.file.query.SearchPageQuery;
import com.ricky.file.query.SearchResponse;
import com.ricky.folder.domain.Folder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.rFilename;
import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_NAME_DUPLICATES;
import static com.ricky.common.utils.CommonUtils.instantToLocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class FileControllerTest extends BaseApiTest {

    @Test
    void should_rename_file() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();

        String newName = rFilename();

        // When
        FileApi.renameFile(manager.getJwt(), fileId, RenameFileCommand.builder()
                .newName(newName)
                .build());

        // Then
        File dbFile = fileRepository.byId(fileId);
        assertEquals(newName, dbFile.getFilename());
    }

    @Test
    void should_delete_file_force() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();
        File dbFile = fileRepository.byId(fileId);

        // When
        FileApi.deleteFileForce(manager.getJwt(), fileId);

        // Then
        assertFalse(fileRepository.exists(dbFile.getId()));
        assertFalse(storageService.exists(dbFile.getStorageId()));
        assertFalse(fileExtraRepository.existsByFileId(dbFile.getId()));

        Folder parentFolder = folderRepository.byId(dbFile.getParentId());
        assertFalse(parentFolder.containsFile(dbFile.getId()));
    }

    @Test
    void should_move_file() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String parentId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String parentId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, parentId1).getFileId();

        // When
        FileApi.moveFile(manager.getJwt(), MoveFileCommand.builder()
                .fileId(fileId)
                .newParentId(parentId2)
                .build());

        // Then
        File dbFile = fileRepository.byId(fileId);
        assertEquals(parentId2, dbFile.getParentId());

        Folder folder1 = folderRepository.byId(parentId1);
        assertFalse(folder1.containsFile(fileId));

        Folder folder2 = folderRepository.byId(parentId2);
        assertTrue(folder2.containsFile(fileId));
    }

    @Test
    void should_fail_to_move_file_if_filename_duplicated() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String parentId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String parentId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId1 = FileUploadApi.upload(manager.getJwt(), file, parentId1).getFileId();
        FileUploadApi.upload(manager.getJwt(), file, parentId2); // 重复文件名占位

        // When & Then
        assertError(() -> FileApi.moveFileRaw(manager.getJwt(), MoveFileCommand.builder()
                .fileId(fileId1)
                .newParentId(parentId2)
                .build()), FILE_NAME_DUPLICATES);
    }

    @Test
    void should_fetch_file_path() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String parentFolderName = rFolderName();
        String childFolderName = rFolderName();
        String parentFolderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, parentFolderName);
        String childFolderId = FolderApi.createFolderWithParent(manager.getJwt(), customId, childFolderName, parentFolderId);

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        String fileId = FileUploadApi.upload(manager.getJwt(), file, childFolderId).getFileId();

        // When
        FilePathResponse response = FileApi.fetchFilePath(manager.getJwt(), customId, fileId);

        // Then
        Folder root = folderRepository.getRoot(customId);
        String path = String.join(NODE_ID_SEPARATOR, root.getFolderName(), parentFolderName, childFolderName, file.getName());
        assertEquals(path, response.getPath());
    }

    @Test
    void should_fetch_file_info() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();

        File dbFile = fileRepository.byId(fileId);

        // When
        FileInfoResponse response = FileApi.fetchFileInfo(manager.getJwt(), fileId);

        // Then
        assertEquals(dbFile.getFilename(), response.getFilename());
        assertEquals(instantToLocalDateTime(dbFile.getCreatedAt()), response.getCreateTime());
        assertEquals(instantToLocalDateTime(dbFile.getUpdatedAt()), response.getUpdateTime());
    }

    @Test
    void should_download() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();
        String fileId = ctx.getFileId();

        // When
        FileApi.DownloadedFile downloaded = FileApi.download(manager.getJwt(), fileId);

        // Then
        File file = fileRepository.byId(fileId);
        assertTrue(downloaded.getContent().length > 0);
        assertEquals(file.getSize(), downloaded.getContent().length);
        assertTrue(downloaded.getContentType().startsWith("text/"));
        assertTrue(downloaded.getContentDisposition().contains(file.getFilename()));
    }

    @Test
    @Disabled
    void should_search() throws IOException {
        // Given
        TestFileContext ctx = setupApi.registerWithFile("testdata/plain-text-file.txt");
        LoginResponse manager = ctx.getManager();

        // When
        SearchResponse response1 = FileApi.search(manager.getJwt(), SearchPageQuery.builder()
                .keyword("file")
                .pageIndex(1)
                .pageSize(10)
                .build());
        SearchResponse response2 = FileApi.search(manager.getJwt(), SearchPageQuery.builder()
                .keyword("To be, or not to be, that is the question")
                .pageIndex(1)
                .pageSize(10)
                .build());

        // Then
        assertEquals(response1.toString(), response2.toString());
    }
}
