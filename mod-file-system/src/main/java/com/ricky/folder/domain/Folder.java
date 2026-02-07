package com.ricky.folder.domain;

import com.ricky.common.domain.hierarchy.HierarchyNode;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folder.domain.event.FolderCreatedEvent;
import com.ricky.folder.domain.event.FolderDeletedEvent;
import com.ricky.folder.domain.event.FolderHierarchyChangedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
import java.util.TreeSet;

import static com.ricky.common.constants.ConfigConstants.FOLDER_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.FOLDER_ID_PREFIX;

/**
 * @brief 文件夹
 */
@Getter
@FieldNameConstants
@Document(FOLDER_COLLECTION)
@TypeAlias(FOLDER_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Folder extends HierarchyNode {

    private String folderName;
    private Set<String> fileIds; // 该文件夹直接child文件ID

    public Folder(String customId, String parentId, String parentPath, String folderName, UserContext userContext) {
        super(newFolderId(), customId, parentId, parentPath, userContext);
        init(folderName, userContext);
    }

    private void init(String folderName, UserContext userContext) {
        this.folderName = folderName;
        this.fileIds = new TreeSet<>();
        raiseEvent(new FolderCreatedEvent(getId(), userContext));
        raiseEvent(new FolderHierarchyChangedEvent(getCustomId(), Set.of(getId()), Set.of(), userContext));
        addOpsLog("新建", userContext);
    }

    public static String newFolderId() {
        return FOLDER_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void addFile(String fileId, UserContext userContext) {
        this.fileIds.add(fileId);
        addOpsLog("新增文件[" + fileId + "]", userContext);
    }

    public void removeFile(String fileId, UserContext userContext) {
        this.fileIds.remove(fileId);
        addOpsLog("移除文件[" + fileId + "]", userContext);
    }

    public boolean containsFile(String fileId) {
        return this.fileIds.contains(fileId);
    }

    public void rename(String newName, UserContext userContext) {
        if (ValidationUtils.equals(this.folderName, newName)) {
            return;
        }

        this.folderName = newName;
        addOpsLog("重命名为[" + newName + "]", userContext);
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new FolderDeletedEvent(this.getId(), userContext));
    }

    public void onMove(Set<String> movedFolderIds, Set<String> movedFileIds, UserContext userContext) {
        raiseEvent(new FolderHierarchyChangedEvent(getCustomId(), movedFolderIds, movedFileIds, userContext));
    }

}
