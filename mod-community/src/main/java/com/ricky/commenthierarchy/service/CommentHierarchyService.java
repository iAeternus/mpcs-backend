package com.ricky.commenthierarchy.service;

import com.ricky.commenthierarchy.command.ReplyCommand;
import com.ricky.commenthierarchy.command.ReplyResponse;
import com.ricky.common.domain.user.UserContext;

public interface CommentHierarchyService {
    ReplyResponse reply(ReplyCommand command, UserContext userContext);
}
