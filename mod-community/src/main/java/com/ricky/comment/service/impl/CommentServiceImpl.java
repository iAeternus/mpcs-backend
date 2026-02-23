package com.ricky.comment.service.impl;

import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentDomainService;
import com.ricky.comment.domain.CommentFactory;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.comment.service.CommentService;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.publicfile.domain.PublicFileDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final RateLimiter rateLimiter;
    private final PublicFileDomainService publicFileDomainService;
    private final CommentDomainService commentDomainService;
    private final CommentFactory commentFactory;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CreateCommentResponse createComment(CreateCommentCommand command, UserContext userContext) {
        rateLimiter.applyFor("Comment:CreateComment", 50);

        publicFileDomainService.checkExists(command.getPostId(), userContext);

        Comment comment = commentFactory.createComment(
                command.getPostId(),
                command.getParentId(),
                command.getContent(),
                userContext
        );
        commentRepository.save(comment);

        log.info("Comment[{}] created", comment.getId());
        return CreateCommentResponse.builder()
                .commentId(comment.getId())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(DeleteCommentCommand command, UserContext userContext) {
        rateLimiter.applyFor("Comment:DeleteComment", 10);

        commentDomainService.deleteComment(command.getPostId(), command.getCommentId(), userContext);
        log.info("Comment[{}] deleted", command.getCommentId());
    }
}
