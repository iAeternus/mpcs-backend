package com.ricky.commenthierarchy.service.impl;

import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentFactory;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.commenthierarchy.command.ReplyCommand;
import com.ricky.commenthierarchy.command.ReplyResponse;
import com.ricky.commenthierarchy.domain.CommentHierarchy;
import com.ricky.commenthierarchy.domain.CommentHierarchyRepository;
import com.ricky.commenthierarchy.service.CommentHierarchyService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.publicfile.domain.PublicFileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentHierarchyServiceImpl implements CommentHierarchyService {

    private final RateLimiter rateLimiter;
    private final PublicFileDomainService publicFileDomainService;
    private final CommentFactory commentFactory;
    private final CommentRepository commentRepository;
    private final CommentHierarchyRepository commentHierarchyRepository;

    @Override
    public ReplyResponse reply(ReplyCommand command, UserContext userContext) {
        rateLimiter.applyFor("CommentHierarchy:Reply", 10);

        publicFileDomainService.checkExists(command.getPostId(), userContext);

        Comment comment = commentFactory.createReplyComment(command.getPostId(), command.getContent(), userContext);
        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(command.getPostId());
        hierarchy.addComment(comment, command.getParentId(), userContext);

        commentRepository.save(comment);
        commentHierarchyRepository.save(hierarchy);

        log.info("Reply[{}] created", comment.getId());
        return ReplyResponse.builder()
                .commentId(comment.getId())
                .commentHierarchyId(hierarchy.getId())
                .build();
    }
}
