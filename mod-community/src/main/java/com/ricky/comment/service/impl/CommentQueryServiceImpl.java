package com.ricky.comment.service.impl;

import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.comment.query.CommentPageQuery;
import com.ricky.comment.query.CommentResponse;
import com.ricky.comment.service.CommentQueryService;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.Pagination;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.ricky.comment.domain.CommentType.FIRST_LEVEL_COMMENT;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public PagedList<CommentResponse> page(CommentPageQuery pageQuery) {
        rateLimiter.applyFor("Comment:Page", 5);

        Pagination pagination = Pagination.pagination(pageQuery.getPageIndex(), pageQuery.getPageSize());
        Query query = query(where("type").is(FIRST_LEVEL_COMMENT));

        long count = mongoTemplate.count(query, Comment.class);
        if (count == 0) {
            return PagedList.pagedList(pagination, (int) count, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(pageQuery));
        query.fields().include("userId", "postId", "content", "createdAt");

        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        return PagedList.pagedList(pagination, (int) count, convert(comments));
    }

    private Sort sort(CommentPageQuery pageQuery) {
        String sortedBy = pageQuery.getSortedBy();
        Sort.Direction direction = pageQuery.getAscSort() ? ASC : DESC;

        if (isBlank(sortedBy)) {
            return by(DESC, "createdAt");
        }

        if (ValidationUtils.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        return by(DESC, "createdAt"); // 暂时不会执行到这里
    }

    private List<CommentResponse> convert(List<Comment> comments) {
        Set<String> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(toImmutableSet());

        Map<String, String> userIdToUsernameMap = userRepository.byIds(userIds).stream()
                .collect(toImmutableMap(User::getId, User::getUsername));

        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .username(userIdToUsernameMap.getOrDefault(comment.getUserId(), "未知用户"))
                        .postId(comment.getPostId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(toImmutableList());
    }
}
