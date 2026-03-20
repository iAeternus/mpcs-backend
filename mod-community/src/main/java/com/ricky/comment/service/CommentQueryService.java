package com.ricky.comment.service;

import com.ricky.comment.query.*;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;

public interface CommentQueryService {
    CommentResponse fetchDetail(String commentId);

    PagedList<CommentResponse> page(CommentPageQuery query);

    PagedList<CommentResponse> pageDirect(DirectReplyPageQuery query);

    PagedList<MyCommentResponse> pageMyComment(MyCommentPageQuery query, UserContext userContext);
}
