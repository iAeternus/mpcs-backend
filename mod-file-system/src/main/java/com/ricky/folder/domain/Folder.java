package com.ricky.folder.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.folder.domain.evt.FolderCreatedEvent;
import com.ricky.folder.domain.evt.FolderDeletedEvent;
import com.ricky.folder.domain.evt.FolderRenamedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Document(FOLDER_COLLECTION)
@TypeAlias(FOLDER_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Folder extends AggregateRoot {

    private String parentId; // 父文件夹ID
    private String folderName;
    private Set<String> fileIds; // 该文件夹下所有文件ID

    private Folder(String parentId, String folderName, UserContext userContext) {
        super(newFolderId(), userContext);
        this.parentId = parentId;
        this.folderName = folderName;
        this.fileIds = new TreeSet<>();
    }

    public static Folder create(String parentId, String folderName, UserContext userContext) {
        Folder folder = new Folder(parentId, folderName, userContext);
        folder.raiseEvent(new FolderCreatedEvent(folder.getId(), userContext));
        return folder;
    }

    public static String newFolderId() {
        return FOLDER_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void rename(String newName, UserContext userContext) {
        if (ValidationUtils.equals(this.folderName, newName)) {
            return;
        }

        this.folderName = newName;
        raiseEvent(new FolderRenamedEvent(this.getId(), userContext));
        addOpsLog("重命名为[" + newName + "]", userContext);
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new FolderDeletedEvent(this.getId(), userContext));
    }
}
