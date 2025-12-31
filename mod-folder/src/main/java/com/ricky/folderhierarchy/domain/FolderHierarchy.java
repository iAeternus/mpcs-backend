package com.ricky.folderhierarchy.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.idtree.IdTreeHierarchy;
import com.ricky.common.domain.idtree.exception.IdNodeLevelOverflowException;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.folderhierarchy.domain.evt.FolderHierarchyChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_TOO_DEEP;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Document(FOLDER_HIERARCHY_COLLECTION)
@TypeAlias(FOLDER_HIERARCHY_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchy extends AggregateRoot {

    private IdTree idTree;
    private IdTreeHierarchy hierarchy;

    private FolderHierarchy(UserContext userContext) {
        super(newFolderHierarchyId(), userContext);
        this.idTree = new IdTree(new ArrayList<>(0));
        this.buildHierarchy();
        addOpsLog("新建", userContext);
    }

    public static FolderHierarchy create(UserContext userContext) {
        return new FolderHierarchy(userContext);
    }

    public static String newFolderHierarchyId() {
        return FOLDER_HIERARCHY_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void update(IdTree idTree, UserContext userContext) {
        this.idTree = idTree;
        this.buildHierarchy();
        raiseEvent(new FolderHierarchyChangedEvent(this.getUserId(), userContext));
        addOpsLog("更新层级", userContext);
    }

    public void addFolder(String parentFolderId, String folderId, UserContext userContext) {
        if (isNotBlank(parentFolderId)) {
            if (!containsFolderId(parentFolderId)) {
                throw new MyException(FOLDER_NOT_FOUND, "未找到文件夹。",
                        "parentFolderId", parentFolderId, "folderId", folderId);
            }

            if (this.hierarchy.levelOf(parentFolderId) >= MAX_FOLDER_HIERARCHY_LEVEL) {
                String msg = "添加失败，文件夹层级最多不能超过" + MAX_FOLDER_HIERARCHY_LEVEL + "层。";
                throw new MyException(FOLDER_HIERARCHY_TOO_DEEP, msg, "userId", this.getUserId());
            }
        }

        this.idTree.addNode(parentFolderId, folderId);
        this.buildHierarchy();
        raiseEvent(new FolderHierarchyChangedEvent(this.getUserId(), userContext));
        addOpsLog("添加文件夹[" + folderId + "]", userContext);
    }

    public void removeFolder(String folderId, UserContext userContext) {
        this.idTree.removeNode(folderId);
        this.buildHierarchy();
        raiseEvent(new FolderHierarchyChangedEvent(this.getUserId(), userContext));
        addOpsLog("删除文件夹[" + folderId + "]", userContext);
    }

    public Set<String> directChildFolderIdsUnder(String parentFolderId) {
        return this.hierarchy.directChildIdsUnder(parentFolderId);
    }

    public Set<String> allSubFolderIdsOf(String folderId) {
        return this.hierarchy.allChildIdsOf(folderId);
    }

    public Map<String, String> folderFullPath(Map<String, String> folderNames) {
        return this.hierarchy.fullNames(folderNames);
    }

    public Set<String> allFolderIds() {
        return this.hierarchy.allIds();
    }

    public Set<String> siblingFolderIdsOf(String folderId) {
        return this.hierarchy.siblingIdsOf(folderId);
    }

    public boolean containsFolderId(String folderId) {
        return this.hierarchy.containsId(folderId);
    }

    private void buildHierarchy() {
        try {
            this.hierarchy = this.idTree.buildHierarchy(MAX_FOLDER_HIERARCHY_LEVEL);
        } catch (IdNodeLevelOverflowException ex) {
            String msg = "文件夹层级最多不能超过" + MAX_FOLDER_HIERARCHY_LEVEL + "层。";
            throw new MyException(FOLDER_HIERARCHY_TOO_DEEP, msg, "userId", this.getUserId());
        }
    }

}
