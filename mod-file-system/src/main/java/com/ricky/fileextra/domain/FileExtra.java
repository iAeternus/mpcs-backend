package com.ricky.fileextra.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.fileextra.domain.event.FileExtraDeletedEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static com.ricky.common.constants.ConfigConstants.FILE_EXTRA_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.FILE_EXTRA_ID_PREFIX;
import static com.ricky.common.utils.ValidationUtils.*;

/**
 * 文件增强信息
 */
@Getter
@TypeAlias("file_extra")
@Document(FILE_EXTRA_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExtra extends AggregateRoot {

    private String fileId; // 对应文件ID
    private String textFileKey; // 提取文本文件的缓存定位键
    private String textFilePath; // 提取出来的文本文件缓存绝对路径
    private String summary; // 文件摘要
    private List<String> keywords; // 关键词列表

    public FileExtra(String fileId, UserContext userContext) {
        super(newFileExtraId(), userContext);
        this.fileId = fileId;
        this.textFileKey = "";
        this.textFilePath = "";
        this.summary = "";
        this.keywords = new ArrayList<>();
        addOpsLog("新建", userContext);
    }

    public static String newFileExtraId() {
        return FILE_EXTRA_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void setTextFileKey(String textFileKey) {
        if (isNotBlank(textFileKey) && notEquals(textFileKey, this.textFileKey)) {
            this.textFileKey = textFileKey;
        }
    }

    public void setTextFilePath(String textFilePath) {
        if (isNotBlank(textFilePath) && notEquals(textFilePath, this.textFilePath)) {
            this.textFilePath = textFilePath;
        }
    }

    public void setSummary(String summary) {
        if (isNotBlank(summary) && notEquals(summary, this.summary)) {
            this.summary = summary;
        }
    }

    public void setKeywords(List<String> keywords) {
        if (isNotEmpty(keywords) && notEquals(keywords, this.keywords)) {
            this.keywords = keywords;
        }
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new FileExtraDeletedEvent(textFileKey, textFilePath, userContext));
    }
}
