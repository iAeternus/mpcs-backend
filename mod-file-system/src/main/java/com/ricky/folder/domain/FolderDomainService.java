package com.ricky.folder.domain;

import com.ricky.common.domain.hierarchy.HierarchyDomainService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static com.ricky.common.utils.ValidationUtils.isEmpty;
import static com.ricky.common.utils.ValidationUtils.nonNull;

@Service
public class FolderDomainService extends HierarchyDomainService<Folder, FolderRepository> {

    private final FileRepository fileRepository;

    @Autowired
    public FolderDomainService(FolderRepository repository, FileRepository fileRepository) {
        super(repository);
        this.fileRepository = fileRepository;
    }

    public void renameFolder(Folder folder, String newName, UserContext userContext) {
        checkSiblingNameDuplication(folder, newName, userContext);
        folder.rename(newName, userContext);
    }

    private void checkSiblingNameDuplication(Folder folder, String newName, UserContext userContext) {
        Set<String> siblingFolderIds = siblingIdsOf(folder.getCustomId(), folder.getId()); // 找兄弟
        if (isEmpty(siblingFolderIds)) {
            return;
        }

        List<String> siblingFolderNames = repository.byIds(siblingFolderIds).stream()
                .map(Folder::getFolderName)
                .collect(toImmutableList());

        if (siblingFolderNames.contains(newName)) {
            throw new MyException(FOLDER_WITH_NAME_ALREADY_EXISTS, "重命名失败，名称已被占用。",
                    "folderId", folder.getId(), "userId", userContext.getUid());
        }
    }

    public DeleteFolderContext collectDeleteFolderContext(String customId, String rootFolderId) {
        Set<String> folderIds = withAllChildIdsOf(customId, rootFolderId);
        List<Folder> folders = repository.byIds(folderIds);

        Set<String> fileIds = folders.stream()
                .flatMap(f -> f.getFileIds().stream())
                .collect(toImmutableSet());
        List<File> files = fileRepository.byIds(fileIds);
        return DeleteFolderContext.builder()
                .folders(folders)
                .files(files)
                .build();
    }

    public MoveResult moveFolder(String customId,
                                 String folderId,
                                 String newParentId,
                                 UserContext userContext) {
        Folder folder = repository.byId(customId, folderId);
        Folder newParent = repository.byId(customId, newParentId);

        checkNoSameNameChild(customId, newParentId, folder.getFolderName());

        List<Folder> subtree = super.moveNode(customId, folder, newParent, userContext);

        Set<String> movedFolderIds = subtree.stream()
                .map(Folder::getId)
                .collect(toImmutableSet());
        Set<String> movedFileIds = subtree.stream()
                .flatMap(f -> f.getFileIds().stream())
                .collect(toImmutableSet());

        Folder root = subtree.stream()
                .filter(f -> ValidationUtils.equals(f.getId(), folderId))
                .findFirst()
                .orElseThrow();
        root.onMove(movedFolderIds, movedFileIds, userContext);

        repository.save(subtree);

        return new MoveResult(movedFolderIds, movedFileIds);
    }

    private void checkNoSameNameChild(String customId, String newParentId, String folderName) {
        if (repository.existsByParentIdAndName(customId, newParentId, folderName)) {
            throw new MyException(FOLDER_NAME_DUPLICATES, "目标目录下存在同名文件夹", "parentId", newParentId, "folderName", folderName);
        }
    }

    public String folderPath(String customId, String folderId) {
        return Arrays.stream(schemaOf(customId, folderId).split(NODE_ID_SEPARATOR))
                .map(repository::cachedById)
                .map(Folder::getFolderName)
                .collect(Collectors.joining(NODE_ID_SEPARATOR));
    }

    public void checkFoldersAllExists(List<String> folderIds) {
        boolean allExists = repository.allExists(folderIds);
        if (!allExists) {
            throw new MyException(NOT_ALL_FOLDERS_EXIST, "有文件夹不存在", "folderIds", folderIds);
        }
    }

    public String resolveParentId(String customId, String parentId) {
        if (nonNull(parentId)) {
            return parentId;
        }
        return repository.getRoot(customId).getId();
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeleteFolderContext {
        List<Folder> folders;
        List<File> files;
    }

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MoveResult {
        Set<String> movedFolderIds;
        Set<String> movedFileIds;
    }

}
