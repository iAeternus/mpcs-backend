package com.ricky.comment.service;

import com.ricky.comment.query.CommentPageQuery;
import com.ricky.comment.query.CommentResponse;
import com.ricky.common.domain.page.PagedList;

public interface CommentQueryService {
    CommentResponse fetchDetail(String commentId);

    PagedList<CommentResponse> page(CommentPageQuery query);
}
