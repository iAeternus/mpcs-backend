package com.ricky.comment.infra;

import com.ricky.comment.domain.Comment;
import com.ricky.comment.domain.CommentRepository;
import com.ricky.common.mongo.MongoBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MongoCommentRepository extends MongoBaseRepository<Comment> implements CommentRepository {

    private final MongoCachedCommentRepository cachedCommentRepository;

    @Override
    public void save(Comment comment) {
        super.save(comment);
        cachedCommentRepository.evictCommentCache(comment.getId());
    }

    @Override
    public Comment byId(String id) {
        return super.byId(id);
    }

    @Override
    public void delete(Comment comment) {
        super.delete(comment);
        cachedCommentRepository.evictCommentCache(comment.getId());
    }

    @Override
    public void delete(List<Comment> comments) {
        super.delete(comments);
        cachedCommentRepository.evictAll();
    }

    @Override
    public List<Comment> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public Comment cachedById(String commentId) {
        return cachedCommentRepository.cachedById(commentId);
    }
}
