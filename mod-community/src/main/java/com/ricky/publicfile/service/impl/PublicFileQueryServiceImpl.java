package com.ricky.publicfile.service.impl;

import com.ricky.common.domain.page.PagedList;
import com.ricky.common.domain.page.Pagination;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.utils.ValidationUtils;
import com.ricky.publicfile.domain.PublicFile;
import com.ricky.publicfile.domain.PublicFileRepository;
import com.ricky.publicfile.query.CommentCountResponse;
import com.ricky.publicfile.query.LikeCountResponse;
import com.ricky.publicfile.query.PublicFilePageQuery;
import com.ricky.publicfile.query.PublicFileResponse;
import com.ricky.publicfile.service.PublicFileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ricky.common.constants.ConfigConstants.*;
import static com.ricky.common.utils.MongoCriteriaUtils.regexSearch;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.common.validation.id.IdValidator.isId;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class PublicFileQueryServiceImpl implements PublicFileQueryService {

    private final RateLimiter rateLimiter;
    private final MongoTemplate mongoTemplate;
    private final PublicFileRepository publicFileRepository;

    @Override
    public PagedList<PublicFileResponse> page(PublicFilePageQuery pageQuery) {
        rateLimiter.applyFor("PublicFile:Page", 5);

        Pagination pagination = Pagination.pagination(pageQuery.getPageIndex(), pageQuery.getPageSize());
        Query query = query(pageCriteria(pageQuery));

        long count = mongoTemplate.count(query, PublicFile.class);
        if (count == 0) {
            return PagedList.pagedList(pagination, (int) count, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.limit()).with(sort(pageQuery));
        query.fields().include("originalFileId", "publisher", "title", "description",
                "likeCount", "commentCount", "createdAt");

        List<PublicFileResponse> publicFiles = mongoTemplate.find(query, PublicFileResponse.class, PUBLIC_FILE_COLLECTION);
        return PagedList.pagedList(pagination, (int) count, publicFiles);
    }

    private Criteria pageCriteria(PublicFilePageQuery pageQuery) {
        Criteria criteria = new Criteria();
        String search = pageQuery.getSearch();
        if (isBlank(search)) {
            return criteria;
        }

        if (isId(search, FILE_ID_PREFIX)) {
            return criteria.and("originalFileId").is(search);
        }

        if (isId(search, USER_ID_PREFIX)) {
            return criteria.and("publisher").is(search);
        }

        return criteria.orOperator(regexSearch("title", search));
    }

    private Sort sort(PublicFilePageQuery pageQuery) {
        String sortedBy = pageQuery.getSortedBy();
        Sort.Direction direction = pageQuery.getAscSort() ? ASC : DESC;

        if (isBlank(sortedBy)) {
            return by(DESC, "createdAt");
        }

        if (ValidationUtils.equals(sortedBy, "createdAt")) {
            return by(direction, "createdAt");
        }

        if (ValidationUtils.equals(sortedBy, "title")) {
            return by(direction, "title");
        }

        if (ValidationUtils.equals(sortedBy, "likeCount")) {
            return by(direction, "likeCount");
        }

        if (ValidationUtils.equals(sortedBy, "commentCount")) {
            return by(direction, "commentCount");
        }

        return by(DESC, "createdAt");
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
