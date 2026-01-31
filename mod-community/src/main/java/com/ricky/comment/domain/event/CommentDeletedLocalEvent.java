package com.ricky.comment.domain.event;

import com.ricky.comment.domain.Comment;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.event.LocalDomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 本地评论删除事件
 * 用于事务提交后立即更新Redis缓存
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentDeletedLocalEvent extends LocalDomainEvent {

    private String postId;

    public CommentDeletedLocalEvent(Comment comment, UserContext userContext) {
        super(comment, userContext);
        this.postId = comment.getPostId();
    }
}