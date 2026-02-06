package com.ricky.comment.domain;

import com.ricky.common.domain.hierarchy.HierarchyRepository;

import java.util.List;
import java.util.Set;

public interface CommentRepository extends HierarchyRepository<Comment> {
    void save(Comment comment);

    Comment byId(String commentId);

    void delete(Comment comment);

    void delete(List<Comment> comments);

    List<Comment> byIds(Set<String> commentIds);

    boolean exists(String commentId);

    Comment cachedById(String commentId);
}
