package com.ricky.publicfile.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.file.domain.File;
import com.ricky.publicfile.domain.event.FilePublishedEvent;
import com.ricky.publicfile.domain.event.FileWithdrewEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.COMMUNITY_POST_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.POST_ID_PREFIX;
import static com.ricky.common.exception.ErrorCodeEnum.PUBLIC_FILE_STATUS_ERROR;
import static com.ricky.publicfile.domain.PublicFileStatus.*;

/**
 * @brief 社区文件
 * @note 文件本体仍然由File聚合管理，这里只是发布行为的`投影聚合`
 */
@Getter
@Document(COMMUNITY_POST_COLLECTION)
@TypeAlias(COMMUNITY_POST_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicFile extends AggregateRoot {

    private String originalFileId; // 对应的文件ID
    private String publisher; // 发布者ID
    private String title; // 展示标题，默认文件名，允许重名
    private String description; // 介绍文字
    private PublicFileStatus status; // 社区文件状态
    private Integer likeCount; // 点赞数
    private Integer commentCount; // 评论数

    public PublicFile(File file, UserContext userContext) {
        super(newPublicFileId(), userContext);
        init(file, userContext);
    }

    private void init(File file, UserContext userContext) {
        this.originalFileId = file.getId();
        this.publisher = userContext.getUid();
        this.title = file.getFilename();
        this.description = "";
        this.status = UNDER_REVIEW;
        this.likeCount = 0;
        this.commentCount = 0;
        raiseEvent(new FilePublishedEvent(getId(), userContext));
        addOpsLog("新建", userContext);
    }

    public static String newPublicFileId() {
        return POST_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void updateTitle(String newTitle, UserContext userContext) {
        if (ValidationUtils.equals(this.title, newTitle)) {
            return;
        }

        this.title = newTitle;
        addOpsLog("更新标题", userContext);
    }

    public void updateDescription(String description, UserContext userContext) {
        if (ValidationUtils.equals(this.description, description)) {
            return;
        }

        this.description = description;
        addOpsLog("编辑介绍", userContext);
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new FileWithdrewEvent(getId(), userContext));
    }

    //
    public void approve(UserContext userContext) {
        if (this.status != UNDER_REVIEW) {
            throw new MyException(PUBLIC_FILE_STATUS_ERROR, "社区文件状态错误。", "postId", getId());
        }
        this.status = PUBLISHED;
        addOpsLog("审查通过，发布成功", userContext);
    }

    public void reject(UserContext userContext) {
        if (this.status != UNDER_REVIEW) {
            throw new MyException(PUBLIC_FILE_STATUS_ERROR, "社区文件状态错误。", "postId", getId());
        }
        this.status = PUBLISH_FAILED;
        addOpsLog("审查未通过，发布失败", userContext);
    }

    public void like() {
        this.likeCount++;
    }

    public void unlike() {
        this.likeCount--;
    }

    public void comment() {
        this.commentCount++;
    }

    public void deleteComment() {
        this.commentCount--;
    }

}
