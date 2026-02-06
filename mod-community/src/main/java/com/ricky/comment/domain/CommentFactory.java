package com.ricky.comment.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.sensitive.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentFactory {

    private final CommentDomainService commentDomainService;
    private final SensitiveWordService sensitiveWordService;

    public Comment createComment(String postId, String parentId, String content, UserContext userContext) {
        String filteredContent = sensitiveWordService.filter(content);
        return new Comment(postId, parentId, commentDomainService.schemaOf(postId, parentId), filteredContent, userContext);
    }

}
