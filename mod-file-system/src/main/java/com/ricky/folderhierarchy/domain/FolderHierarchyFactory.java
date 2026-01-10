package com.ricky.folderhierarchy.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.common.domain.SpaceType.PERSONAL;
import static com.ricky.common.domain.SpaceType.PUBLIC;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static com.ricky.folderhierarchy.domain.FolderHierarchy.defaultCustomId;

@Component
@RequiredArgsConstructor
public class FolderHierarchyFactory {

    private final FolderHierarchyRepository folderHierarchyRepository;

    /**
     * 创建个人空间
     */
    public FolderHierarchy createPersonalSpace(UserContext userContext) {
        return new FolderHierarchy(defaultCustomId(PERSONAL), userContext);
    }

    /**
     * 创建团队空间
     */
    public FolderHierarchy createTeamSpace(String customId, UserContext userContext) {
        checkCustomIdDuplication(customId, userContext.getUid());
        return new FolderHierarchy(customId, userContext);
    }

    /**
     * 创建公共空间
     */
    public FolderHierarchy createPublicSpace(UserContext userContext) {
        return new FolderHierarchy(defaultCustomId(PUBLIC), userContext);
    }

    private void checkCustomIdDuplication(String customId, String userId) {
        if (folderHierarchyRepository.cachedExistsByCustomId(customId, userId)) {
            throw new MyException(FOLDER_HIERARCHY_WITH_CUSTOM_ID_ALREADY_EXISTS, "自定义编号已被占用。", "customId", customId);
        }
    }

}
