package com.ricky.comment.service.impl;

import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.comment.query.*;
import com.ricky.comment.service.CommentQueryService;
import com.ricky.common.domain.page.MongoPage;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.SortRegistry;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.common.constants.ConfigConstants.COMMENT_COLLECTION;

@Service
@RequiredArgsConstructor
public class CommentQueryServiceImpl implements CommentQueryService {

    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public CommentResponse fetchDetail(String commentId) {
        rateLimiter.applyFor("Comment:FetchDetail", 50);

        Comment comment = commentRepository.cachedById(commentId);
        User user = userRepository.cachedById(comment.getUserId());
        return CommentResponse.builder()
                .username(user.getUsername())
                .postId(comment.getCustomId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public PagedList<CommentResponse> page(CommentPageQuery pageQuery) {
        rateLimiter.applyFor("Comment:Page", 5);

        return MongoPage.of(Comment.class, COMMENT_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and("customId").is(pageQuery.getPostId())
                        .and("parentId").isNull())
                .sort(SortRegistry.newInstance().resolve(pageQuery.getSortedBy(), pageQuery.getAscSort()))
                .project("userId", "customId", "content", "createdAt")
                .map(this::toCommentResponse, mongoTemplate);
    }

    @Override
    public PagedList<CommentResponse> pageDirect(DirectReplyPageQuery pageQuery) {
        rateLimiter.applyFor("Comment:PageDirect", 5);

        return MongoPage.of(Comment.class, COMMENT_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and("parentId").is(pageQuery.getParentId()))
                .sort(SortRegistry.newInstance().resolve(pageQuery.getSortedBy(), pageQuery.getAscSort()))
                .project("userId", "customId", "content", "createdAt")
                .map(this::toCommentResponse, mongoTemplate);
    }

    private List<CommentResponse> toCommentResponse(List<Comment> comments) {
        Set<String> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(toImmutableSet());

        Map<String, String> userIdToUsernameMap = userRepository.byIds(userIds).stream()
                .collect(toImmutableMap(User::getId, User::getUsername));

        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .username(userIdToUsernameMap.getOrDefault(comment.getUserId(), "未知用户"))
                        .postId(comment.getCustomId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(toImmutableList());
    }

    @Override
    public PagedList<MyCommentResponse> pageMyComment(MyCommentPageQuery pageQuery, UserContext userContext) {
        rateLimiter.applyFor("Comment:PageMyComment", 5);

        return MongoPage.of(Comment.class, COMMENT_COLLECTION)
                .pageQuery(pageQuery)
                .where(c -> c.and("userId").is(userContext.getUid()))
                .sort(SortRegistry.newInstance().resolve(pageQuery.getSortedBy(), pageQuery.getAscSort()))
                .project("customId", "content", "createdAt")
                .map(this::toMyCommentResponse, mongoTemplate);
    }

    private List<MyCommentResponse> toMyCommentResponse(List<Comment> comments) {
        return comments.stream()
                .map(comment -> MyCommentResponse.builder()
                        .postId(comment.getCustomId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(toImmutableList());
    }
}
