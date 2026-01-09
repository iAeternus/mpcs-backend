package com.ricky.folderhierarchy.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.SpaceType;
import com.ricky.common.domain.idtree.IdTree;
import com.ricky.common.domain.idtree.IdTreeHierarchy;
import com.ricky.common.domain.idtree.exception.IdNodeLevelOverflowException;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.folder.domain.Folder;
import com.ricky.folderhierarchy.domain.event.FolderHierarchyChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.domain.SpaceType.*;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_HIERARCHY_TOO_DEEP;
import static com.ricky.common.exception.ErrorCodeEnum.FOLDER_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 文件层级结构，一个层级结构就是一个文件空间（file space）
 * 用户可拥有多个文件空间
 */
@Getter
@Document(FOLDER_HIERARCHY_COLLECTION)
@TypeAlias(FOLDER_HIERARCHY_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderHierarchy extends AggregateRoot {

    private String customId; // 自定义ID，全局唯一
    private IdTree idTree;
    private IdTreeHierarchy hierarchy;

    public FolderHierarchy(String customId, UserContext userContext) {
        super(newFolderHierarchyId(), userContext);
        init(customId, userContext);
    }

    private void init(String customId, UserContext userContext) {
        this.customId = customId;
        this.idTree = new IdTree(new ArrayList<>(0));
        this.buildHierarchy();
        addOpsLog("新建", userContext);
    }

    public static String newFolderHierarchyId() {
        return FOLDER_HIERARCHY_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public static String defaultCustomId(SpaceType spaceType) {
        String uuid = UuidGenerator.newShortUuid();
        return switch (spaceType) {
            case PERSONAL -> PERSONAL.getPrefix() + uuid;
            case PUBLIC -> PUBLIC.getPrefix() + uuid;
            default -> throw new IllegalStateException("团队空间customId应由用户指定");
        };
    }

    public void update(IdTree idTree, UserContext userContext) {
        this.idTree = idTree;
        this.buildHierarchy();
        raiseEvent(new FolderHierarchyChangedEvent(this.getUserId(), userContext));
        addOpsLog("更新层级", userContext);
    }

    public void addFolder(Folder folder, UserContext userContext) {
        String folderId = folder.getId();
        String parentFolderId = folder.getParentId();
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

    public Set<String> withAllSubFolderIdsOf(String folderId) {
        return this.hierarchy.withAllChildIdsOf(folderId);
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

    public String schemaOf(String folderId) {
        return hierarchy.schemaOf(folderId);
    }

    public Set<String> withAllParentIdsOf(String folderId) {
        return hierarchy.withAllParentIdsOf(folderId);
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
