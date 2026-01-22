package com.ricky.commenthierarchy.domain;

public interface CommentHierarchyRepository {
    void save(CommentHierarchy commentHierarchy);

    CommentHierarchy byPostId(String postId);
}
