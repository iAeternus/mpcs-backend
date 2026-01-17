package com.ricky.like.service.impl;

import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.like.domain.CachedLikeRepository;
import com.ricky.like.domain.LikedCount;
import com.ricky.like.query.LikedCountResponse;
import com.ricky.like.service.LikeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeQueryServiceImpl implements LikeQueryService {

    private final RateLimiter rateLimiter;
    private final CachedLikeRepository cachedLikeRepository;

    @Override
    public LikedCountResponse fetchLikedCount(String postId) {
        rateLimiter.applyFor("Like:FetchLikedCount", 100);
        LikedCount likedCount = cachedLikeRepository.cachedById(postId);
        return LikedCountResponse.builder()
                .postId(likedCount.getPostId())
                .count(likedCount.getCount())
                .build();
    }
}
