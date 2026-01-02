package com.ricky.folder.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_WITH_NAME_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class FolderDomainService {

    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    public void renameFolder(Folder folder, String newName, UserContext userContext) {
        FolderHierarchy hierarchy = folderHierarchyRepository.byUserId(userContext.getUid());
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

}
