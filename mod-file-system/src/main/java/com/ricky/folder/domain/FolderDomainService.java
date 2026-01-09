package com.ricky.folder.domain;

import com.ricky.common.domain.SpaceType;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_WITH_NAME_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class FolderDomainService {

    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;
    private final FileRepository fileRepository;

    public void renameFolder(String customId, Folder folder, String newName, UserContext userContext) {
        FolderHierarchy hierarchy = folderHierarchyRepository.cachedByCustomId(customId);
        checkSiblingNameDuplication(folder, hierarchy, newName, userContext.getUid());
        folder.rename(newName, userContext);
    }

    private void checkSiblingNameDuplication(Folder folder, FolderHierarchy folderHierarchy, String newName, String userId) {
        Set<String> siblingFolderIds = folderHierarchy.siblingFolderIdsOf(folder.getId()); // 找兄弟
        if (ValidationUtils.isEmpty(siblingFolderIds)) {
            return;
        }

        List<String> siblingFolderNames = folderRepository.cachedUserAllFolders(userId).stream()
                .filter(cachedFolder -> siblingFolderIds.contains(cachedFolder.getId()))
                .map(UserCachedFolder::getFolderName)
                .collect(toImmutableList());

        if (siblingFolderNames.contains(newName)) {
            throw new MyException(FOLDER_WITH_NAME_ALREADY_EXISTS, "重命名失败，名称已被占用。",
                    "folderId", folder.getId(), "userId", userId);
        }
    }

    public Set<String> collectAllFileIds(String rootFolderId, FolderHierarchy hierarchy) {
        Set<String> folderIds = hierarchy.withAllSubFolderIdsOf(rootFolderId);
        List<Folder> folders = folderRepository.byIds(folderIds);

        return folders.stream()
                .flatMap(f -> f.getFileIds().stream())
                .collect(toImmutableSet());
    }

    public DeleteFolderContext collectContext(String rootFolderId, FolderHierarchy hierarchy) {
        Set<String> folderIds = hierarchy.withAllSubFolderIdsOf(rootFolderId);
        List<Folder> folders = folderRepository.byIds(folderIds);

        Set<String> fileIds = folders.stream()
                .flatMap(f -> f.getFileIds().stream())
                .collect(toImmutableSet());
        List<File> files = fileRepository.byIds(fileIds);
        return DeleteFolderContext.builder()
                .folders(folders)
                .files(files)
                .build();
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeleteFolderContext {
        List<Folder> folders;
        List<File> files;
    }

}
