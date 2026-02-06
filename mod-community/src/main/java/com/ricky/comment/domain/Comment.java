package com.ricky.comment.domain;

import com.ricky.comment.domain.event.CommentCreatedLocalEvent;
import com.ricky.comment.domain.event.CommentDeletedLocalEvent;
import com.ricky.common.domain.hierarchy.HierarchyNode;
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
public class Comment extends HierarchyNode {

    private String content;
    // TODO 以后增加对评论的点赞，分页可以按照最热排序

    public Comment(String postId, String parentId, String parentPath, String content, UserContext userContext) {
        super(newCommentId(), postId, parentId, parentPath, userContext);
        init(content, userContext);
    }

    private void init(String content, UserContext userContext) {
        this.content = content;
        raiseLocalEvent(new CommentCreatedLocalEvent(this, userContext));
        addOpsLog("新建", userContext);
    }

    public static String newCommentId() {
        return COMMENT_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void onDelete(UserContext userContext) {
        raiseLocalEvent(new CommentDeletedLocalEvent(this, userContext));
    }

}
