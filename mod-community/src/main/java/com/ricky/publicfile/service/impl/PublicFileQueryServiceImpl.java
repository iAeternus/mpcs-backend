package com.ricky.publicfile.service.impl;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.page.MongoPageQuery;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.SortRegistry;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.validation.id.IdValidator.isId;

@Service
@RequiredArgsConstructor
public class PublicFileQueryServiceImpl implements PublicFileQueryService {

    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final PublicFileRepository publicFileRepository;

    @Override
    public PagedList<PublicFileResponse> page(PublicFilePageQuery pageQuery) {
        rateLimiter.applyFor("PublicFile:Page", 5);

        return MongoPageQuery.of(PublicFile.class, PUBLIC_FILE_COLLECTION)
                .pageQuery(pageQuery)
                .noSpecifiedField()
                .search((search, c, q) -> {
                    if (isId(search, FILE_ID_PREFIX)) {
                        return c.and("originalFileId").is(search);
                    }
                    if (isId(search, USER_ID_PREFIX)) {
                        return c.and("publisher").is(search);
                    }
                    return c.orOperator(Criteria.where("title").regex(search));
                })
                .sort(SortRegistry.newInstance()
                        .register("createdAt", "createdAt")
                        .register("title", "title")
                        .register("likeCount", "likeCount")
                        .register("commentCount", "commentCount"))
                .project("_id", "originalFileId", "publisher", "title", "description",
                        "likeCount", "commentCount", "createdAt")
                .fetchAs(PublicFileResponse.class, mongoTemplate);
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

    @Override
    public PagedList<PublicFileResponse> pageMy(PublicFilePageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("PublicFile:Page", 5);

        return MongoPageQuery.of(PublicFile.class, PUBLIC_FILE_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and(AggregateRoot.Fields.createdBy).is(userContext.getUid()))
                .search((search, c, q) -> {
                    if (isId(search, FILE_ID_PREFIX)) {
                        return c.and("originalFileId").is(search);
                    }
                    if (isId(search, USER_ID_PREFIX)) {
                        return c.and("publisher").is(search);
                    }
                    return c.orOperator(Criteria.where("title").regex(search));
                })
                .sort(SortRegistry.newInstance()
                        .register("createdAt", "createdAt")
                        .register("title", "title")
                        .register("likeCount", "likeCount")
                        .register("commentCount", "commentCount"))
                .project("_id", "originalFileId", "publisher", "title", "description",
                        "likeCount", "commentCount", "createdAt")
                .fetchAs(PublicFileResponse.class, mongoTemplate);
    }
}
