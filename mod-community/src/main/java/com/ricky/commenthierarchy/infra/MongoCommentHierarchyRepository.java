package com.ricky.commenthierarchy.infra;

import com.ricky.commenthierarchy.domain.CommentHierarchy;
import com.ricky.commenthierarchy.domain.CommentHierarchyRepository;
import com.ricky.common.exception.MyException;
import com.ricky.common.mongo.MongoBaseRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.ricky.common.exception.ErrorCodeEnum.COMMENT_HIERARCHY_NOT_FOUND;
import static com.ricky.common.utils.ValidationUtils.isNull;
import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MongoCommentHierarchyRepository extends MongoBaseRepository<CommentHierarchy> implements CommentHierarchyRepository {

    @Override
    public void save(CommentHierarchy commentHierarchy) {
        super.save(commentHierarchy);
    }

    @Override
    public CommentHierarchy byPostId(String postId) {
        requireNotBlank(postId, "Public file ID must not be blank");

        Query query = query(where("postId").is(postId));
        CommentHierarchy hierarchy = mongoTemplate.findOne(query, CommentHierarchy.class);
        if (isNull(hierarchy)) {
            throw new MyException(COMMENT_HIERARCHY_NOT_FOUND, "评论层次结构不存在", "postId", postId);
        }

        return hierarchy;
    }
}
