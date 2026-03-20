package com.ricky.like.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.like.query.LikeStatusQuery;
import com.ricky.like.query.LikeStatusesResponse;
import com.ricky.like.query.LikedCountResponse;

public interface LikeQueryService {
    LikedCountResponse fetchLikedCount(String postId);

    LikeStatusesResponse fetchLikeStatus(LikeStatusQuery query, UserContext userContext);
}
