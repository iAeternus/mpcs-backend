package com.ricky.file.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.OpsLogTypeEnum;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.file.domain.evt.FileUploadedEvent;
import com.ricky.file.domain.metadata.Metadata;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstant.FILE_COLLECTION;
import static com.ricky.common.constants.ConfigConstant.FILE_ID_PREFIX;

/**
 * @brief 文件
 */
@Getter
@TypeAlias("file")
@Document(FILE_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class File extends AggregateRoot {

    private String ownerId;
    private String teamId; // 若属于团队则记录团队ID
    private String parentId; // 父文件夹ID，根目录也是文件夹
    private StorageId storageId; // 文件内容存储ID
    private String filename;
    private Metadata metadata;
    private String path; // 存储路径，暂时只支持绝对路径
    private FileStatus status;

    private File(String ownerId, String parentId, StorageId storageId, String filename, Metadata metaData, String path) {
        super(newFileId());
        this.ownerId = ownerId;
        this.teamId = null;
        this.parentId = parentId;
        this.storageId = storageId;
        this.filename = filename;
        this.metadata = metaData;
        this.path = path;
        this.status = FileStatus.NORMAL;
        addOpsLog(OpsLogTypeEnum.CREATE, "Create File");
    }

    public static File create(String ownerId, String parentId, StorageId storageId, String filename, Metadata metaData, String path) {
        File file = new File(ownerId, parentId, storageId, filename, metaData, path);
        file.raiseEvent(new FileUploadedEvent(
                file.getId(),
                file.filename,
                file.metadata.getHash(),
                file.metadata.getSize(),
                file.metadata.getMimeType()
        ));
        return file;
    }

    public static String newFileId() {
        return FILE_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

}
