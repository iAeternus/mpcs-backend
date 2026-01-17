package com.ricky.like.service;

import com.ricky.common.domain.user.UserContext;

public interface LikeService {
    void like(String postId, UserContext userContext);

    void unlike(String postId, UserContext userContext);
}
