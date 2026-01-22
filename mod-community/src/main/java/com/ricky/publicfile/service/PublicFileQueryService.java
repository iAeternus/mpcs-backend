package com.ricky.publicfile.service;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.publicfile.query.CommentCountResponse;
import com.ricky.publicfile.query.LikeCountResponse;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;

public interface PublicFileQueryService {
    PagedList<PublicFileResponse> page(PublicFilePageQuery query, UserContext userContext);

    CommentCountResponse fetchCommentCount(String postId);

    LikeCountResponse fetchLikeCount(String postId);
}
