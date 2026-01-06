package com.ricky.folder;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.File;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.UserCachedFolder;
import com.ricky.folder.domain.dto.cmd.CreateFolderCommand;
import com.ricky.folder.domain.dto.cmd.RenameFolderCommand;
import com.ricky.folder.domain.evt.FolderCreatedEvent;
import com.ricky.folder.domain.evt.FolderDeletedEvent;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.evt.FolderHierarchyChangedEvent;
import com.ricky.upload.FileUploadApi;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static com.ricky.RandomTestFixture.rFolderName;
import static com.ricky.common.constants.ConfigConstants.MAX_FOLDER_HIERARCHY_LEVEL;
import static com.ricky.common.event.DomainEventType.*;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

public class FolderControllerTest extends BaseApiTest {

    @Test
    void should_create_folder() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderName = rFolderName();

        // When
        String folderId = FolderApi.createFolder(response.getJwt(), folderName);

        // Then
        Folder folder = folderRepository.byId(folderId);
        assertEquals(folderName, folder.getFolderName());

        FolderCreatedEvent evt1 = latestEventFor(folderId, FOLDER_CREATED, FolderCreatedEvent.class);
        assertEquals(folderId, evt1.getFolderId());

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(response.getUserId());
        assertEquals(1, hierarchy.allFolderIds().size());
        assertTrue(hierarchy.containsFolderId(folderId));

        FolderHierarchyChangedEvent evt2 = latestEventFor(hierarchy.getId(), FOLDER_HIERARCHY_CHANGED, FolderHierarchyChangedEvent.class);
        assertEquals(response.getUserId(), evt2.getUserId());
    }

    @Test
    void should_create_folder_with_parent() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String parentFolderId = FolderApi.createFolder(response.getJwt(), rFolderName());

        // When
        String subFolderId = FolderApi.createFolder(response.getJwt(), CreateFolderCommand.builder()
                .folderName(rFolderName())
                .parentId(parentFolderId)
                .build());

        // Then
        Folder subFolder = folderRepository.byId(subFolderId);
        assertNotNull(subFolder);

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(response.getUserId());
        assertTrue(hierarchy.containsFolderId(subFolderId));
        assertTrue(hierarchy.containsFolderId(parentFolderId));
    }

    @Test
    void should_create_folder_with_same_name_but_different_level() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderName = rFolderName();

        // When
        String parentFolderId = FolderApi.createFolder(response.getJwt(), folderName);
        String subFolderId = FolderApi.createFolder(response.getJwt(), CreateFolderCommand.builder()
                .folderName(folderName)
                .parentId(parentFolderId)
                .build());

        // Then
        assertEquals(folderName, folderRepository.byId(parentFolderId).getFolderName());
        assertEquals(folderName, folderRepository.byId(subFolderId).getFolderName());

        List<UserCachedFolder> userCachedFolders = folderRepository.cachedUserAllFolders(response.getUserId());
        assertEquals(2, userCachedFolders.size());
    }

    @Test
    void should_fail_to_create_folder_if_parent_folder_not_exist() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();

        // When & Then
        assertError(() -> FolderApi.createFolderRaw(response.getJwt(), CreateFolderCommand.builder()
                .folderName(rFolderName())
                .parentId(Folder.newFolderId())
                .build()), FOLDER_NOT_FOUND);
    }

    @Test
    void should_fail_to_create_folder_if_hierarchy_too_deep() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String parentFolderId = FolderApi.createFolder(response.getJwt(), rFolderName());
        for (int i = 0; i < MAX_FOLDER_HIERARCHY_LEVEL - 1; ++i) {
            parentFolderId = FolderApi.createFolderWithParent(response.getJwt(), rFolderName(), parentFolderId);
        }

        // When & Then
        String parentId = parentFolderId;
        assertError(() -> FolderApi.createFolderRaw(response.getJwt(), CreateFolderCommand.builder()
                .folderName(rFolderName())
                .parentId(parentId)
                .build()), FOLDER_HIERARCHY_TOO_DEEP);
    }

    @Test
    void should_fail_to_create_folder_if_name_already_exists() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        CreateFolderCommand command = CreateFolderCommand.builder().folderName(rFolderName()).build();
        FolderApi.createFolder(response.getJwt(), command);

        // When & Then
        assertError(() -> FolderApi.createFolderRaw(response.getJwt(), command), FOLDER_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    void should_rename_folder() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderId = FolderApi.createFolder(response.getJwt(), rFolderName());
        String newName = rFolderName();

        // When
        FolderApi.renameFolder(response.getJwt(), folderId, RenameFolderCommand.builder()
                .newName(newName)
                .build());

        // Then
        Folder folder = folderRepository.byId(folderId);
        assertEquals(newName, folder.getFolderName());
    }

    @Test
    void should_rename_folder_with_same_name_but_different_level() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderName = rFolderName();
        String parentFolderId = FolderApi.createFolder(response.getJwt(), folderName);
        String subFolderId = FolderApi.createFolderWithParent(response.getJwt(), rFolderName(), parentFolderId);

        // When
        FolderApi.renameFolder(response.getJwt(), subFolderId, RenameFolderCommand.builder()
                .newName(folderName)
                .build());

        // Then
        assertEquals(folderName, folderRepository.byId(parentFolderId).getFolderName());
    }

    @Test
    void should_fail_to_rename_folder_if_name_already_exists_at_same_level() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderName = rFolderName();
        String folderId1 = FolderApi.createFolder(response.getJwt(), folderName);
        String folderId2 = FolderApi.createFolder(response.getJwt(), rFolderName());

        // When & Then
        assertError(() -> FolderApi.renameFolderRaw(response.getJwt(), folderId2, RenameFolderCommand.builder()
                .newName(folderName)
                .build()), FOLDER_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    void should_delete_folder_force() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderId = FolderApi.createFolder(response.getJwt(), rFolderName());
        assertTrue(folderHierarchyRepository.byUserId(response.getUserId()).containsFolderId(folderId));

        // When
        FolderApi.deleteFolderForce(response.getJwt(), folderId);

        // Then
        assertFalse(folderRepository.exists(folderId));

        FolderDeletedEvent evt = latestEventFor(folderId, FOLDER_DELETED, FolderDeletedEvent.class);
        assertEquals(folderId, evt.getFolderId());

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(response.getUserId());
        assertFalse(hierarchy.containsFolderId(folderId));

        FolderHierarchyChangedEvent evt2 = latestEventFor(hierarchy.getId(), FOLDER_HIERARCHY_CHANGED, FolderHierarchyChangedEvent.class);
        assertEquals(response.getUserId(), evt2.getUserId());
    }

    @Test
    public void delete_folder_force_should_also_delete_sub_folders() {
        // Given
        LoginResponse response = setupApi.registerWithLogin();
        String folderId = FolderApi.createFolder(response.getJwt(), rFolderName());
        String subFolderId = FolderApi.createFolderWithParent(response.getJwt(), rFolderName(), folderId);

        // When
        FolderApi.deleteFolderForce(response.getJwt(), folderId);

        // Then
        assertFalse(folderRepository.exists(subFolderId));
        assertEquals(subFolderId, latestEventFor(subFolderId, FOLDER_DELETED, FolderDeletedEvent.class).getFolderId());
        assertEquals(folderId, latestEventFor(folderId, FOLDER_DELETED, FolderDeletedEvent.class).getFolderId());
    }

    @Test
    public void delete_folder_force_should_also_delete_sub_files() throws IOException, InterruptedException {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String folderId = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);

        String fileId = FileUploadApi.upload(loginResponse.getJwt(), file, folderId).getFileId();
        File dbFile = fileRepository.byId(fileId);

        // 这里必须sleep，直接删除会导致文件上传事件还未处理完，导致mongodb写冲突
        Thread.sleep(5 * 1000);

        // When
        FolderApi.deleteFolderForce(loginResponse.getJwt(), folderId);

        // Then
        assertFalse(fileRepository.exists(dbFile.getId()));
        assertFalse(fileStorage.exists(dbFile.getStorageId()));
        assertFalse(fileExtraRepository.existsByFileId(dbFile.getId()));
    }

//    @Test
//    public void should_cache_folders() {
//        LoginResponse response = setupApi.registerWithLogin();
//        FolderApi.createFolder(response.getJwt(), rFolderName());
//        String key = "Cache:USER_FOLDERS::" + response.getUserId();
//
//        assertEquals(FALSE, stringRedisTemplate.hasKey(key));
//
//        DepartmentHierarchyApi.fetchDepartmentHierarchy(response.getJwt());
//        assertEquals(TRUE, stringRedisTemplate.hasKey(key));
//
//        DepartmentApi.createDepartment(response.getJwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build());
//        assertEquals(FALSE, stringRedisTemplate.hasKey(key));
//    }

}
