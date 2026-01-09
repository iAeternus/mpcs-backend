package com.ricky.folderhierarchy.domain;

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
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.ricky.common.domain.SpaceType.*;
import static com.ricky.common.domain.idtree.IdTree.NODE_ID_SEPARATOR;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NAME_DUPLICATES;
import static com.ricky.management.MpcsManageUser.MPCS_MANAGE_USER_ID;

@Service
@RequiredArgsConstructor
public class FolderHierarchyDomainService {

    private final FolderRepository folderRepository;
    private final FolderHierarchyRepository folderHierarchyRepository;

    public void updateFolderHierarchy(FolderHierarchy hierarchy, IdTree idTree, UserContext userContext) {
        hierarchy.update(idTree, userContext);

        List<UserCachedFolder> cachedFolders = folderRepository.cachedUserAllFolders(userContext.getUid());
        Map<String, String> allFolderNames = cachedFolders.stream()
                .collect(toImmutableMap(UserCachedFolder::getId, UserCachedFolder::getFolderName));
        Map<String, String> allFullPaths = hierarchy.folderFullPath(allFolderNames);
        if (allFullPaths.size() > Set.copyOf(allFullPaths.values()).size()) {
            throw new MyException(FOLDER_NAME_DUPLICATES, "更新失败，存在名称重复。", "userId", userContext.getUid());
        }
    }

//    public List<FolderHierarchy> spaceOf(SpaceType spaceType, String userId) {
//        return switch (spaceType) {
//            case PERSONAL -> List.of(personalSpaceOf(userId));
//            case TEAM -> teamSpaceOf(userId);
//            case PUBLIC -> List.of(publicSpace());
//        };
//    }

    public FolderHierarchy publicSpace() {
        List<FolderHierarchy> folderHierarchies = folderHierarchyRepository.byUserIdAndSpaceType(MPCS_MANAGE_USER_ID, PUBLIC);
        if (folderHierarchies.size() != 1) {
            throw new IllegalStateException("公共空间必须有且仅有一个");
        }
        return folderHierarchies.get(0);
    }

    public FolderHierarchy personalSpaceOf(String userId) {
        List<FolderHierarchy> folderHierarchies = folderHierarchyRepository.byUserIdAndSpaceType(userId, PERSONAL);
        if (folderHierarchies.size() != 1) {
            throw new IllegalStateException("个人空间每个用户必须有且仅有一个");
        }
        return folderHierarchies.get(0);
    }

    public List<FolderHierarchy> teamSpaceOf(String userId) {
        return folderHierarchyRepository.byUserIdAndSpaceType(userId, TEAM);
    }

    /**
     * 计算folderId的所有祖先ID，包含自身，并有序排列<br>
     * 顺序：[self, parent, parent.parent, ..., root]
     */
    public List<String> withAllParentIdsOf(String customId, String folderId) {
        FolderHierarchy hierarchy = folderHierarchyRepository.cachedByCustomId(customId);
        String[] folderIds = hierarchy.schemaOf(folderId).split(NODE_ID_SEPARATOR);
        return IntStream.range(0, folderIds.length)
                .map(i -> folderIds.length - 1 - i)  // 逆序索引
                .mapToObj(i -> folderIds[i])
                .collect(toImmutableList());
    }

}
