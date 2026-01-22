package com.ricky.comment.service;

import com.ricky.comment.command.CreateCommentCommand;
import com.ricky.comment.command.CreateCommentResponse;
import com.ricky.comment.command.DeleteCommentCommand;
import com.ricky.common.domain.user.UserContext;

public interface CommentService {
    CreateCommentResponse createComment(CreateCommentCommand command, UserContext userContext);

    void deleteComment(DeleteCommentCommand command, UserContext userContext);
}
