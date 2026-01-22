package com.ricky.comment.domain;

import java.util.Map;

public interface CachedCommentRepository {

    /**
     * 增加发布物评论数
     */
    void increaseCommentCount(String postId, Integer delta);

    /**
     * 更新发布物评论数
     */
    void updateCommentCount(String postId, Integer newCommentCount);

    /**
     * 获取发布物评论数
     */
    CommentCount cachedById(String postId);

    /**
     * 获取所有发布物评论数
     */
    Map<String, Integer> listAllCommentCount();

}
