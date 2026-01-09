package com.ricky.folder.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_WITH_NAME_ALREADY_EXISTS;

@Component
@RequiredArgsConstructor
public class FolderFactory {

    private final FolderRepository folderRepository;

    public Folder create(String folderName,
                         String userId,
                         String parentFolderId,
                         FolderHierarchy folderHierarchy,
                         UserContext userContext) {
        checkSiblingNameDuplication(folderName, userId, parentFolderId, folderHierarchy);
        return new Folder(parentFolderId, folderName, userContext);
    }

    private void checkSiblingNameDuplication(String folderName,
                                             String userId,
                                             String parentFolderId, // 可能为null
                                             FolderHierarchy folderHierarchy) {
        Set<String> siblingFolderIds = folderHierarchy.directChildFolderIdsUnder(parentFolderId); // 找父节点的直接孩子
        if (ValidationUtils.isEmpty(siblingFolderIds)) {
            return;
        }

        List<String> siblingFolderNames = folderRepository.cachedUserAllFolders(userId).stream()
                .filter(cachedFolder -> siblingFolderIds.contains(cachedFolder.getId()))
                .map(UserCachedFolder::getFolderName)
                .collect(toImmutableList());

        if (siblingFolderNames.contains(folderName)) {
            throw new MyException(FOLDER_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    "folderName", folderName, "userId", userId);
        }
    }

}
