package com.ricky.folder.domain;

import com.google.common.collect.ImmutableList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_WITH_NAME_ALREADY_EXISTS;
import static com.ricky.common.utils.ValidationUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class FolderFactory {

    private final FolderDomainService folderDomainService;
    private final FolderRepository folderRepository;

    public Folder create(String customId, String parentId, String folderName, UserContext userContext) {
        checkSiblingNameDuplication(customId, parentId, folderName, userContext);
        return new Folder(customId, parentId, folderDomainService.schemaOf(customId, parentId), folderName, userContext);
    }

    private void checkSiblingNameDuplication(String customId, String parentId, String folderName, UserContext userContext) {
        Set<String> siblingFolderIds = folderDomainService.directChildIdsUnder(customId, parentId); // 找父节点的直接孩子
        if (isEmpty(siblingFolderIds)) {
            return;
        }

        List<String> siblingFolderNames = folderRepository.byIds(siblingFolderIds).stream()
                .map(Folder::getFolderName)
                .collect(toImmutableList());

        if (siblingFolderNames.contains(folderName)) {
            throw new MyException(FOLDER_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    "folderName", folderName, "userId", userContext.getUid());
        }
    }

    // 根文件夹父节点为null，文件夹名为customId
    public Folder createRoot(String customId, UserContext userContext) {
        return create(customId, null, customId, userContext);
    }

}
