package com.ricky.folderhierarchy;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.domain.idtree.IdTree;
import com.ricky.folder.FolderApi;
import com.ricky.folder.command.RenameFolderCommand;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.command.UpdateFolderHierarchyCommand;
import com.ricky.folderhierarchy.query.FolderHierarchyResponse;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.RandomTestFixture.rFolderName;
import static com.ricky.common.constants.ConfigConstants.FOLDER_HIERARCHY_CACHE;
import static com.ricky.common.constants.ConfigConstants.MAX_FOLDER_HIERARCHY_LEVEL;
import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static com.ricky.common.event.DomainEventType.FOLDER_HIERARCHY_CHANGED;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_TOO_DEEP;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NAME_DUPLICATES;
import static com.ricky.common.utils.CommonUtils.redisCacheKey;
import static com.ricky.folderhierarchy.query.FolderHierarchyResponse.HierarchyFolder;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

public class FolderHierarchyControllerTest extends BaseApiTest {

    @Test
    void should_fetch_folder_hierarchy() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String folderId1 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());
        String folderId2 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());
        String folderId3 = FolderApi.createFolderWithParent(loginResponse.getJwt(), rFolderName(), folderId1);

        // When
        FolderHierarchyResponse response = FolderHierarchyApi.fetchFolderHierarchy(loginResponse.getJwt());

        // Then
        List<String> folderIds = response.getAllFolders().stream()
                .map(HierarchyFolder::getId)
                .collect(toImmutableList());
        assertEquals(3, folderIds.size());
        assertTrue(folderIds.containsAll(List.of(folderId1, folderId2, folderId3)));

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(loginResponse.getUserId());
        assertEquals(hierarchy.getIdTree(), response.getIdTree());
    }

    @Test
    void should_update_folder_hierarchy() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String folderId1 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());
        String folderId2 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());
        String folderId3 = FolderApi.createFolderWithParent(loginResponse.getJwt(), rFolderName(), folderId1);

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, folderId2);
        idTree.addNode(null, folderId3);
        idTree.addNode(folderId2, folderId1);

        // When
        FolderHierarchyApi.updateFolderHierarchy(loginResponse.getJwt(), UpdateFolderHierarchyCommand.builder()
                .idTree(idTree)
                .build());

        // Then
        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(loginResponse.getUserId());
        assertEquals(3, hierarchy.allFolderIds().size());
        assertTrue(hierarchy.allFolderIds().containsAll(List.of(folderId1, folderId2, folderId3)));
        assertEquals(folderId2 + NODE_ID_SEPARATOR + folderId1, hierarchy.getHierarchy().schemaOf(folderId1));
        assertEquals(folderId2, hierarchy.getHierarchy().schemaOf(folderId2));
        assertEquals(folderId3, hierarchy.getHierarchy().schemaOf(folderId3));

        var evt = latestEventFor(hierarchy.getId(), FOLDER_HIERARCHY_CHANGED, FolderHierarchyChangedEvent.class);
        assertEquals(loginResponse.getUserId(), evt.getUserId());
    }

    @Test
    void should_fail_to_update_folder_hierarchy_if_too_deep() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        List<String> folderIds = IntStream.range(0, MAX_FOLDER_HIERARCHY_LEVEL + 1)
                .mapToObj(i -> FolderApi.createFolder(loginResponse.getJwt(), rFolderName()))
                .collect(toImmutableList());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        IntStream.range(0, folderIds.size())
                .forEach(i -> {
                    String parentId = i == 0 ? null : folderIds.get(i - 1);
                    String currId = folderIds.get(i);
                    idTree.addNode(parentId, currId);
                });

        UpdateFolderHierarchyCommand command = UpdateFolderHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        // When & Then
        assertError(() -> FolderHierarchyApi.updateFolderHierarchyRaw(loginResponse.getJwt(), command), FOLDER_HIERARCHY_TOO_DEEP);
    }

    @Test
    void should_fail_to_update_folder_hierarchy_if_name_duplicates_at_root_level() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String folderName = rFolderName();
        String folderId1 = FolderApi.createFolder(loginResponse.getJwt(), folderName);
        String folderId2 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, folderId1);
        idTree.addNode(folderId1, folderId2);

        FolderHierarchyApi.updateFolderHierarchy(loginResponse.getJwt(), UpdateFolderHierarchyCommand.builder()
                .idTree(idTree)
                .build());
        FolderApi.renameFolder(loginResponse.getJwt(), folderId2, RenameFolderCommand.builder()
                .newName(folderName)
                .build());

        IdTree updateIdTree = new IdTree(new ArrayList<>(0));
        updateIdTree.addNode(null, folderId1);
        updateIdTree.addNode(null, folderId2);

        UpdateFolderHierarchyCommand command = UpdateFolderHierarchyCommand.builder()
                .idTree(updateIdTree)
                .build();

        // When & Then
        assertError(() -> FolderHierarchyApi.updateFolderHierarchyRaw(loginResponse.getJwt(), command), FOLDER_NAME_DUPLICATES);
    }

    @Test
    void should_fail_to_update_folder_hierarchy_if_name_duplicates_at_none_root_level() {
        // Given
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String folderName = rFolderName();
        String folderId1 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());
        String folderId2 = FolderApi.createFolder(loginResponse.getJwt(), folderName);
        String folderId3 = FolderApi.createFolder(loginResponse.getJwt(), rFolderName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, folderId1);
        idTree.addNode(folderId1, folderId2);
        idTree.addNode(folderId2, folderId3);

        FolderHierarchyApi.updateFolderHierarchy(loginResponse.getJwt(), UpdateFolderHierarchyCommand.builder()
                .idTree(idTree)
                .build());
        FolderApi.renameFolder(loginResponse.getJwt(), folderId3, RenameFolderCommand.builder()
                .newName(folderName)
                .build());

        IdTree updateIdTree = new IdTree(new ArrayList<>(0));
        updateIdTree.addNode(null, folderId1);
        updateIdTree.addNode(folderId1, folderId2);
        updateIdTree.addNode(folderId1, folderId3);

        UpdateFolderHierarchyCommand command = UpdateFolderHierarchyCommand.builder()
                .idTree(updateIdTree)
                .build();

        // When & Then
        assertError(() -> FolderHierarchyApi.updateFolderHierarchyRaw(loginResponse.getJwt(), command), FOLDER_NAME_DUPLICATES);
    }

    @Test
    public void should_cache_folder_hierarchy() {
        LoginResponse response = setupApi.registerWithLogin();
        String key = redisCacheKey(FOLDER_HIERARCHY_CACHE, response.getUserId());
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        folderHierarchyRepository.cachedByUserId(response.getUserId());
        assertTrue(stringRedisTemplate.hasKey(key));

        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(response.getUserId());
        folderHierarchyRepository.save(hierarchy);
        assertFalse(stringRedisTemplate.hasKey(key));
    }

}
