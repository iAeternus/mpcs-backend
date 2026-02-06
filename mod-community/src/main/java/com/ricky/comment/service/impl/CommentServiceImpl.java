package com.ricky.comment.service.impl;

import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentFactory;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.comment.service.CommentService;
import com.ricky.commenthierarchy.domain.CommentHierarchy;
import com.ricky.commenthierarchy.domain.CommentHierarchyRepository;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.publicfile.domain.PublicFileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final RateLimiter rateLimiter;
    private final PublicFileDomainService publicFileDomainService;
    private final CommentFactory commentFactory;
    private final CommentRepository commentRepository;
    private final CommentHierarchyRepository commentHierarchyRepository;

    @Override
    @Transactional
    public CreateCommentResponse createComment(CreateCommentCommand command, UserContext userContext) {
        rateLimiter.applyFor("Comment:CreateComment", 50);

        publicFileDomainService.checkExists(command.getPostId(), userContext);

        Comment comment = commentFactory.createFirstLevelComment(command.getPostId(), command.getContent(), userContext);
        CommentHierarchy commentHierarchy = new CommentHierarchy(comment.getPostId(), userContext);
        commentHierarchy.addComment(comment, "", userContext);

        commentRepository.save(comment);
        commentHierarchyRepository.save(commentHierarchy);

        log.info("Comment[{}] created", comment.getId());
        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .commentHierarchyId(commentHierarchy.getId())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(DeleteCommentCommand command, UserContext userContext) {
        rateLimiter.applyFor("Comment:DeleteComment", 10);

        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(command.getPostId());
        Set<String> commentIds = hierarchy.withAllChildIdsOf(command.getCommentId());
        List<Comment> comments = commentRepository.byIds(commentIds);

        comments.forEach(comment -> comment.onDelete(userContext));
        hierarchy.removeComment(command.getCommentId(), userContext);

        commentRepository.delete(comments);
        commentHierarchyRepository.save(hierarchy);

        log.info("Comment[{}] deleted", command.getCommentId());
    }
}
