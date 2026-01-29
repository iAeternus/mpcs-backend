package com.ricky.commenthierarchy.service.impl;

import com.ricky.comment.domain.Comment;
import com.ricky.commenthierarchy.domain.CommentHierarchyRepository;
import com.ricky.commenthierarchy.query.ReplyPageQuery;
import com.ricky.commenthierarchy.query.ReplyPageResponse;
import com.ricky.commenthierarchy.service.CommentHierarchyQueryService;
import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.Pagination;
import com.ricky.common.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static com.ricky.comment.domain.CommentType.REPLY_COMMENT;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class CommentHierarchyQueryServiceImpl implements CommentHierarchyQueryService {

    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final CommentHierarchyRepository commentHierarchyRepository;

    @Override
    public PagedList<ReplyPageResponse> pageReply(ReplyPageQuery pageQuery) {
        rateLimiter.applyFor("CommentHierarchy:PageReply", 5);

        // TODO 不改存储模型只能查出所有的评论，分页的意义何在？
//        CommentHierarchy hierarchy = commentHierarchyRepository.byPostId(pageQuery.getPostId());
//        hierarchy.withAllChildIdsOf()

        Pagination pagination = Pagination.pagination(pageQuery.getPageIndex(), pageQuery.getPageSize());
        Query query = query(where("type").is(REPLY_COMMENT));

        long count = mongoTemplate.count(query, Comment.class);


        return null;
    }
}
