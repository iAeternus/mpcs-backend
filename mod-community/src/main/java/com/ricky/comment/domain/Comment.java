package com.ricky.comment.domain;

import com.ricky.comment.domain.event.LocalCommentCreatedEvent;
import com.ricky.comment.domain.event.LocalCommentDeletedEvent;
import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.utils.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.ricky.common.constants.ConfigConstants.COMMENT_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.COMMENT_ID_PREFIX;

@Getter
@Document(COMMENT_COLLECTION)
@TypeAlias(COMMENT_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends AggregateRoot {

    private String postId; // 发布物ID
    private String content;
    private CommentType type; // 评论类型
    // TODO 以后增加对评论的点赞，分页可以按照最热排序

    public Comment(String postId, String content, CommentType type, UserContext userContext) {
        super(newCommentId(), userContext);
        init(postId, content, type, userContext);
    }

    private void init(String postId, String content, CommentType type, UserContext userContext) {
        this.postId = postId;
        this.content = content;
        this.type = type;
        raiseLocalEvent(new LocalCommentCreatedEvent(this, userContext));
        addOpsLog("新建", userContext);
    }

    public static String newCommentId() {
        return COMMENT_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void onDelete(UserContext userContext) {
        raiseLocalEvent(new LocalCommentDeletedEvent(this, userContext));
    }

}
