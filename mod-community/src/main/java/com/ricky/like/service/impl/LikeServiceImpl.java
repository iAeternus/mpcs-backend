package com.ricky.like.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.like.domain.CachedLikeRepository;
import com.ricky.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final RateLimiter rateLimiter;
    private final CachedLikeRepository cachedLikeRepository;

    // like/unlike只有redis操作，无需加事务
    @Override
    public void like(String postId, UserContext userContext) {
        rateLimiter.applyFor("Like:Like", 100);
        boolean changed = cachedLikeRepository.tryLike(userContext.getUid(), postId);
        if (changed) {
            log.info("User[{}] like PublicFile[{}]", userContext.getUid(), postId);
        }
    }

    @Override
    public void unlike(String postId, UserContext userContext) {
        rateLimiter.applyFor("Like:Unlike", 100);
        boolean changed = cachedLikeRepository.tryUnlike(userContext.getUid(), postId);
        if (changed) {
            log.info("User[{}] unlike PublicFile[{}]", userContext.getUid(), postId);
        }
    }
}
