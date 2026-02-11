package com.ricky.apitest.folder;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.login.LoginApi;
import com.ricky.apitest.upload.FileUploadApi;
import com.ricky.apitest.user.UserApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.File;
import com.ricky.folder.command.*;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.event.FolderCreatedEvent;
import com.ricky.folder.domain.event.FolderDeletedEvent;
import com.ricky.folder.domain.event.FolderHierarchyChangedEvent;
import com.ricky.folder.query.FolderContentResponse;
import com.ricky.upload.domain.event.FileUploadedLocalEvent;
import com.ricky.user.query.UserInfoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static com.ricky.apitest.RandomTestFixture.rFolderName;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.event.DomainEventType.*;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

public class FolderControllerTest extends BaseApiTest {

    @Test
    void should_create_folder() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderName = rFolderName();

        // When
        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, folderName);

        // Then
        Folder folder = folderRepository.byId(folderId);
        assertEquals(folderName, folder.getFolderName());

        Folder root = folderRepository.getRoot(customId);
        assertEquals(root.getId(), folder.getParentId());

        List<Folder> folders = folderRepository.getAllByCustomId(customId);
        assertEquals(2, folders.size()); // 用户默认创建一个根文件夹

        assertTrue(folderDomainService.containsId(customId, folderId));

        FolderCreatedEvent evt1 = latestEventFor(folderId, FOLDER_CREATED, FolderCreatedEvent.class);
        assertEquals(folderId, evt1.getFolderId());

        FolderHierarchyChangedEvent evt2 = latestEventFor(folder.getId(), FOLDER_HIERARCHY_CHANGED, FolderHierarchyChangedEvent.class);
        assertEquals(folder.getCustomId(), evt2.getCustomId());
    }

    @Test
    void should_create_folder_with_parent() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String parentFolderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        // When
        String subFolderId = FolderApi.createFolder(manager.getJwt(), CreateFolderCommand.builder()
                .customId(customId)
                .folderName(rFolderName())
                .parentId(parentFolderId)
                .build());

        // Then
        Folder subFolder = folderRepository.byId(subFolderId);
        assertNotNull(subFolder);

        assertTrue(folderDomainService.containsId(customId, subFolderId));
        assertTrue(folderDomainService.containsId(customId, parentFolderId));
    }

    @Test
    void should_create_folder_with_same_name_but_different_level() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderName = rFolderName();

        // When
        String parentFolderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, folderName);
        String subFolderId = FolderApi.createFolder(manager.getJwt(), CreateFolderCommand.builder()
                .customId(customId)
                .folderName(folderName)
                .parentId(parentFolderId)
                .build());

        // Then
        assertEquals(folderName, folderRepository.byId(parentFolderId).getFolderName());
        assertEquals(folderName, folderRepository.byId(subFolderId).getFolderName());

        List<Folder> folders = folderRepository.getAllByCustomId(customId);
        assertEquals(3, folders.size());
        assertTrue(folderDomainService.containsId(customId, subFolderId));
        assertTrue(folderDomainService.containsId(customId, parentFolderId));
    }

    @Test
    void should_fail_to_create_folder_if_parent_folder_not_exist() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        // When & Then
        assertError(() -> FolderApi.createFolderRaw(manager.getJwt(), CreateFolderCommand.builder()
                .customId(customId)
                .folderName(rFolderName())
                .parentId(Folder.newFolderId())
                .build()), AR_NOT_FOUND);
    }

//    @Test
//    void should_fail_to_create_folder_if_hierarchy_too_deep() {
//        // Given
//        LoginResponse manager = setupApi.registerWithLogin();
//        String customId = personalCustomId(manager.getUserId());
//
//        String parentFolderId = FolderApi.createFolder(manager.getJwt(), customId, rFolderName());
//        for (int i = 0; i < MAX_FOLDER_HIERARCHY_LEVEL - 1; ++i) {
//            parentFolderId = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), parentFolderId);
//        }
//
//        // When & Then
//        String parentId = parentFolderId;
//        assertError(() -> FolderApi.createFolderRaw(manager.getJwt(), CreateFolderCommand.builder()
//                .customId(customId)
//                .folderName(rFolderName())
//                .parentId(parentId)
//                .build()), FOLDER_HIERARCHY_TOO_DEEP);
//    }

    @Test
    void should_fail_to_create_folder_if_name_already_exists() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        Folder root = folderRepository.getRoot(customId);
        CreateFolderCommand command = CreateFolderCommand.builder()
                .customId(customId)
                .parentId(root.getId())
                .folderName(rFolderName())
                .build();
        FolderApi.createFolder(manager.getJwt(), command);

        // When & Then
        assertError(() -> FolderApi.createFolderRaw(manager.getJwt(), command), FOLDER_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    void should_rename_folder() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String newName = rFolderName();

        // When
        FolderApi.renameFolder(manager.getJwt(), folderId, RenameFolderCommand.builder()
                .customId(customId)
                .newName(newName)
                .build());

        // Then
        Folder folder = folderRepository.byId(folderId);
        assertEquals(newName, folder.getFolderName());
    }

    @Test
    void should_rename_folder_with_same_name_but_different_level() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderName = rFolderName();
        String parentFolderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, folderName);
        String subFolderId = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), parentFolderId);


        // When
        FolderApi.renameFolder(manager.getJwt(), subFolderId, RenameFolderCommand.builder()
                .customId(customId)
                .newName(folderName)
                .build());

        // Then
        assertEquals(folderName, folderRepository.byId(parentFolderId).getFolderName());
    }

    @Test
    void should_fail_to_rename_folder_if_name_already_exists_at_same_level() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderName = rFolderName();
        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, folderName);
        String folderId2 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());


        // When & Then
        assertError(() -> FolderApi.renameFolderRaw(manager.getJwt(), folderId2, RenameFolderCommand.builder()
                .customId(customId)
                .newName(folderName)
                .build()), FOLDER_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    void should_delete_folder_force() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        // When
        FolderApi.deleteFolderForce(manager.getJwt(), folderId, DeleteFolderForceCommand.builder()
                .customId(customId)
                .build());

        // Then
        assertFalse(folderRepository.exists(folderId));
        assertFalse(folderDomainService.containsId(customId, folderId));

        FolderHierarchyChangedEvent evt2 = latestEventFor(folderId, FOLDER_HIERARCHY_CHANGED, FolderHierarchyChangedEvent.class);
        assertEquals(customId, evt2.getCustomId());
    }

    @Test
    public void delete_folder_force_should_also_delete_sub_folders() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String subFolderId = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId);

        // When
        FolderApi.deleteFolderForce(manager.getJwt(), folderId, DeleteFolderForceCommand.builder()
                .customId(customId)
                .build());

        // Then
        assertFalse(folderRepository.exists(subFolderId));
        assertEquals(subFolderId, latestEventFor(subFolderId, FOLDER_DELETED, FolderDeletedEvent.class).getFolderId());
        assertEquals(folderId, latestEventFor(folderId, FOLDER_DELETED, FolderDeletedEvent.class).getFolderId());
    }

    @Test
    public void delete_folder_force_should_also_delete_sub_files() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);

        String fileId = FileUploadApi.upload(manager.getJwt(), file, folderId).getFileId();
        File dbFile = fileRepository.byId(fileId);

        // 这里必须等待，直接删除会导致文件上传事件还未处理完，导致mongodb写冲突
        awaitLatestLocalEventConsumed(fileId, FileUploadedLocalEvent.class);

        // When
        FolderApi.deleteFolderForce(manager.getJwt(), folderId, DeleteFolderForceCommand.builder()
                .customId(customId)
                .build());

        // Then
        assertFalse(fileRepository.exists(dbFile.getId()));
        assertFalse(storageService.exists(dbFile.getStorageId()));
        assertFalse(fileExtraRepository.existsByFileId(dbFile.getId()));
    }

    @Test
    void should_fail_to_delete_force_if_delete_root() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());
        Folder root = folderRepository.getRoot(customId);

        // When & Then
        assertError(() -> FolderApi.deleteFolderForceRaw(manager.getJwt(), root.getId(), DeleteFolderForceCommand.builder()
                .customId(customId)
                .build()), CANNOT_DELETE_ROOT_FOLDER);
    }

    @Test
    void should_move_folder() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String folderId2 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId1);
        String folderId3 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId1);
        String folderId4 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId2);
        String folderId5 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId4);

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, folderId4).getFileId();

        // When
        MoveFolderResponse response = FolderApi.moveFolder(manager.getJwt(), MoveFolderCommand.builder()
                .customId(customId)
                .folderId(folderId4)
                .newParentId(folderId3)
                .build());

        // Then
        assertEquals(2, response.getMovedFolderCount());
        assertEquals(1, response.getMovedFileCount());

        Folder folder4 = folderRepository.byId(folderId4);
        assertEquals(folderId3, folder4.getParentId());

        Folder folder5 = folderRepository.byId(folderId5);
        assertEquals(folderId4, folder5.getParentId());

        File dbFile = fileRepository.byId(fileId);
        assertEquals(folderId4, dbFile.getParentId());
    }

    @Test
    void should_move_folder_to_root() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String folderId2 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId1);

        // When
        FolderApi.moveFolder(manager.getJwt(), MoveFolderCommand.builder()
                .customId(customId)
                .folderId(folderId2)
                .build());

        // Then
        Folder root = folderRepository.getRoot(customId);
        Folder folder2 = folderRepository.byId(folderId2);
        assertEquals(root.getId(), folder2.getParentId());
    }

    @Test
    void should_fail_to_move_folder_if_folder_name_duplicated() {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderName = rFolderName();
        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, folderName);
        String folderId2 = FolderApi.createFolderWithParent(manager.getJwt(), customId, folderName, folderId1);

        // When & Then
        assertError(() -> FolderApi.moveFolderRaw(manager.getJwt(), MoveFolderCommand.builder()
                .customId(customId)
                .folderId(folderId2)
                .build()), FOLDER_NAME_DUPLICATES);
    }

    @Test
    void should_fetch_folder_content() throws IOException {
        // Given
        LoginResponse manager = setupApi.registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        String folderId1 = setupApi.createFolderUnderRoot(manager.getJwt(), customId, rFolderName());
        String folderId2 = FolderApi.createFolderWithParent(manager.getJwt(), customId, rFolderName(), folderId1);

        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File file = resource.getFile();
        setupApi.deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, folderId1).getFileId();

        // When
        FolderContentResponse response = FolderApi.fetchFolderContent(manager.getJwt(), customId, folderId1);

        // Then
        assertEquals(1, response.getFolders().size());
        assertEquals(1, response.getFiles().size());
        assertEquals(folderId2, response.getFolders().get(0).getId());
        assertEquals(fileId, response.getFiles().get(0).getId());
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

    @Test
    void front_end_data() {
        String token = LoginApi.loginWithMobileOrEmail("w_ziwei2004@163.com", "Wzw_757723");
        UserInfoResponse myUserInfo = UserApi.myUserInfo(token);

        Folder root = folderRepository.getRoot(myUserInfo.getCustomId());

//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test4", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test5", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test6", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test7", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test8", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test9", root.getId());
//        FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test10", root.getId());

//        String test1Test1Id = FolderApi.createFolderWithParent(token, myUserInfo.getCustomId(), "Test1Test1", test1Id);
    }

}
