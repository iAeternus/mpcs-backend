package com.ricky.folder.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private String ownerId;
    private String teamId; // 若属于团队则记录团队ID
    private String parentId; // 父文件夹ID
    private String folderName;

    public static String newFolderId() {
        return FOLDER_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

}
