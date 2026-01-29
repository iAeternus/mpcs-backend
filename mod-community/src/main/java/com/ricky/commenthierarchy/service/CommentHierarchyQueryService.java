package com.ricky.commenthierarchy.service;

import com.ricky.commenthierarchy.query.ReplyPageQuery;
import com.ricky.commenthierarchy.query.ReplyPageResponse;
import com.ricky.common.domain.page.PagedList;

public interface CommentHierarchyQueryService {
    PagedList<ReplyPageResponse> pageReply(ReplyPageQuery query);
}
