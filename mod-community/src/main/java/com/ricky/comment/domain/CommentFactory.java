package com.ricky.comment.domain;

import com.ricky.common.domain.user.UserContext;
import org.springframework.stereotype.Component;

import static com.ricky.comment.domain.CommentType.FIRST_LEVEL_COMMENT;
import static com.ricky.comment.domain.CommentType.REPLY_COMMENT;

@Component
public class CommentFactory {

    public Comment createFirstLevelComment(String postId, String content, UserContext userContext) {
        return new Comment(postId, content, FIRST_LEVEL_COMMENT, userContext);
    }

    public Comment createReplyComment(String postId, String content, UserContext userContext) {
        return new Comment(postId, content, REPLY_COMMENT, userContext);
    }

}
