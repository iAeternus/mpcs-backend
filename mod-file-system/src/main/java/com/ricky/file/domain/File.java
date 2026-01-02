package com.ricky.file.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.file.domain.evt.FileDeletedEvent;
import com.ricky.file.domain.evt.FileUploadedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.FILE_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.FILE_ID_PREFIX;

/**
 * @brief 文件 <br>
 * 1. 同一个hash值或storageId的文件在GridFs中保证唯一，多个文件可能对应一个hash值，
 *    这些文件可能分属不同用户，但是只要有一个用户修改了文件，系统立即认为hash值不同，则存储新文件，
 *    于是不会发生冲突
 * 2. 若已知parentId，整个路径已经确定，无需创建任何Folder。通过createFolder和upload可以组合多种操作
 */
@Getter
@TypeAlias("file")
@Document(FILE_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends AggregateRoot {

    private String parentId; // 父文件夹ID，根目录也是文件夹
    private StorageId storageId; // 文件内容存储ID
    private String filename;
    private long size; // 文件大小，单位：byte
    private String hash; // 文件hash值
    private FileStatus status;

    private File(String parentId, StorageId storageId, String filename, long size, String hash, UserContext userContext) {
        super(newFileId(), userContext);
        this.parentId = parentId;
        this.storageId = storageId;
        this.filename = filename;
        this.size = size;
        this.hash = hash;
        this.status = FileStatus.NORMAL;
        addOpsLog("新建", userContext);
    }

    public static File create(String parentId, StorageId storageId, String filename, long size, String hash, UserContext userContext) {
        File file = new File(parentId, storageId, filename, size, hash, userContext);
        file.raiseEvent(new FileUploadedEvent(
                file.getId(),
                file.filename,
                file.getHash(),
                file.getSize(),
                userContext
        ));
        return file;
    }

    public static String newFileId() {
        return FILE_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new FileDeletedEvent(this.getId(), userContext));
    }

}
