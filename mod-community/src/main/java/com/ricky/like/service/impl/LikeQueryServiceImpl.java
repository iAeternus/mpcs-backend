package com.ricky.like.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.like.domain.CachedLikeRepository;
import com.ricky.like.domain.Like;
import com.ricky.like.domain.LikeStatus;
import com.ricky.like.domain.LikedCount;
import com.ricky.like.query.LikeStatusQuery;
import com.ricky.like.query.LikeStatusesResponse;
import com.ricky.like.query.LikedCountResponse;
import com.ricky.like.service.LikeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.common.constants.ConfigConstants.LIKE_COLLECTION;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class LikeQueryServiceImpl implements LikeQueryService {

    private final RateLimiter rateLimiter;
    private final CachedLikeRepository cachedLikeRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public LikedCountResponse fetchLikedCount(String postId) {
        rateLimiter.applyFor("Like:FetchLikedCount", 100);
        LikedCount likedCount = cachedLikeRepository.cachedById(postId);
        return LikedCountResponse.builder()
                .postId(likedCount.getPostId())
                .count(likedCount.getCount())
                .build();
    }

    @Override
    public LikeStatusesResponse fetchLikeStatus(LikeStatusQuery query, UserContext userContext) {
        rateLimiter.applyFor("Like:FetchLikeStatus", 50);

        Map<String, Boolean> likedMap = new HashMap<>(query.getPostIds().size());

        Query mongoQuery = query(where("likerId").is(userContext.getUid()).and("postId").in(query.getPostIds()))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        mongoTemplate.find(mongoQuery, Like.class, LIKE_COLLECTION)
                .forEach(like -> likedMap.putIfAbsent(like.getPostId(), like.getStatus() == LikeStatus.LIKE));

        Map<String, Boolean> cachedLikeMap = cachedLikeRepository.listLikeStatus(query.getPostIds(), userContext.getUid());
        likedMap.putAll(cachedLikeMap);

        return LikeStatusesResponse.builder()
                .statuses(likedMap.keySet().stream()
                        .map(postId -> LikeStatusesResponse.LikeStatus.builder()
                                .postId(postId)
                                .liked(likedMap.get(postId))
                                .build()
                        )
                        .collect(toImmutableList()))
                .build();
    }
}
