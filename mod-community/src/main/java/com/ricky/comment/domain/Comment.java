package com.ricky.comment.domain;

import com.ricky.comment.domain.event.CommentCreatedEvent;
import com.ricky.comment.domain.event.CommentDeletedEvent;
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

    public Comment(String postId, String content, UserContext userContext) {
        super(newCommentId(), userContext);
        init(postId, content, userContext);
    }

    private void init(String postId, String content, UserContext userContext) {
        this.postId = postId;
        this.content = content;
        raiseEvent(new CommentCreatedEvent(getPostId(), userContext));
        addOpsLog("新建", userContext);
    }

    public static String newCommentId() {
        return COMMENT_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void onDelete(UserContext userContext) {
        raiseEvent(new CommentDeletedEvent(getPostId(), userContext));
    }

}
