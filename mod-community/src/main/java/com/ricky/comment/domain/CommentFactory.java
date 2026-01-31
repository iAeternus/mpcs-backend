package com.ricky.comment.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.sensitive.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.comment.domain.CommentType.FIRST_LEVEL_COMMENT;
import static com.ricky.comment.domain.CommentType.REPLY_COMMENT;

@Component
@RequiredArgsConstructor
public class CommentFactory {

    private final SensitiveWordService sensitiveWordService;

    public Comment createFirstLevelComment(String postId, String content, UserContext userContext) {
        String filteredContent = sensitiveWordService.filter(content);
        return new Comment(postId, filteredContent, FIRST_LEVEL_COMMENT, userContext);
    }

    public Comment createReplyComment(String postId, String content, UserContext userContext) {
        String filteredContent = sensitiveWordService.filter(content);
        return new Comment(postId, filteredContent, REPLY_COMMENT, userContext);
    }

}
