package com.ricky.like.service;

import com.ricky.like.query.LikedCountResponse;

public interface LikeQueryService {
    LikedCountResponse fetchLikedCount(String postId);
}
