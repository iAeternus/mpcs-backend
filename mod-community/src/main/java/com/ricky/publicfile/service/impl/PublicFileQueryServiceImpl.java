package com.ricky.publicfile.service.impl;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import com.ricky.publicfile.query.CommentCountResponse;
import com.ricky.publicfile.query.LikeCountResponse;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import com.ricky.publicfile.service.PublicFileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicFileQueryServiceImpl implements PublicFileQueryService {

    private final RateLimiter rateLimiter;
    private final PublicFileRepository publicFileRepository;

    @Override
    public PagedList<PublicFileResponse> page(PublicFilePageQuery query, UserContext userContext) {
        // TODO
        return null;
    }

    @Override
    public CommentCountResponse fetchCommentCount(String postId) {
        rateLimiter.applyFor("PublicFile:FetchCommentCount", 50);

        PublicFile publicFile = publicFileRepository.cachedById(postId);
        return CommentCountResponse.builder()
                .commentCount(publicFile.getCommentCount())
                .build();
    }

    @Override
    public LikeCountResponse fetchLikeCount(String postId) {
        rateLimiter.applyFor("PublicFile:FetchLikeCount", 50);

        PublicFile publicFile = publicFileRepository.cachedById(postId);
        return LikeCountResponse.builder()
                .likeCount(publicFile.getLikeCount())
                .build();
    }
}
