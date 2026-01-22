package com.ricky.comment.service.impl;

import com.ricky.comment.query.CommentPageQuery;
import com.ricky.comment.query.CommentResponse;
import com.ricky.comment.service.CommentQueryService;
import com.ricky.common.domain.page.PagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentQueryServiceImpl implements CommentQueryService {
    @Override
    public CommentResponse fetchDetail(String commentId) {
        return null;
    }

    @Override
    public PagedList<CommentResponse> page(CommentPageQuery query) {
        return null;
    }
}
