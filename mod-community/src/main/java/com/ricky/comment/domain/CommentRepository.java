package com.ricky.comment.domain;

import java.util.List;
import java.util.Set;

public interface CommentRepository {
    void save(Comment comment);

    Comment byId(String commentId);

    void delete(Comment comment);

    void delete(List<Comment> comments);

    List<Comment> byIds(Set<String> commentIds);

    boolean exists(String commentId);

    Comment cachedById(String commentId);
}
