package com.ricky.folderhierarchy.domain;

import com.google.common.collect.ImmutableMap;
import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.folder.domain.UserCachedFolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NAME_DUPLICATES;

@Service
@RequiredArgsConstructor
public class FolderHierarchyDomainService {

    private final FolderRepository folderRepository;

    public void updateFolderHierarchy(FolderHierarchy hierarchy, IdTree idTree, UserContext userContext) {
        hierarchy.update(idTree, userContext);

        List<UserCachedFolder> cachedFolders = folderRepository.cachedUserAllFolders(userContext.getUid());
        ImmutableMap<String, String> allFolderNames = cachedFolders.stream()
                .collect(toImmutableMap(UserCachedFolder::getId, UserCachedFolder::getFolderName));
        Map<String, String> allFullPaths = hierarchy.folderFullPath(allFolderNames);
        if (allFullPaths.size() > Set.copyOf(allFullPaths.values()).size()) {
            throw new MyException(FOLDER_NAME_DUPLICATES, "更新失败，存在名称重复。", "userId", userContext.getUid());
        }
    }
}
