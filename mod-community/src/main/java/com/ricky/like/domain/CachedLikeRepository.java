package com.ricky.like.domain;

import com.ricky.common.validation.collection.NoNullElement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public interface CachedLikeRepository {

    /**
     * 尝试点赞，若状态发生变化，则发布物被点赞数 + 1
     *
     * @return true 如果状态发生变化
     */
    boolean tryLike(String userId, String postId);

    /**
     * 尝试取消点赞，若状态发生变化，则发布物被点赞数 - 1
     *
     * @return true 如果状态发生变化
     */
    boolean tryUnlike(String userId, String postId);

    /**
     * 根据发布物ID获取被点赞数
     */
    LikedCount cachedById(String postId);

    /**
     * 获取缓存中所有点赞记录
     */
    List<LikeRecord> listAllLike();

    /**
     * 获取缓存中所有发布物被点赞数
     */
    List<LikedCount> listAllLikedCount();

    /**
     * 获取缓存中指定用户对指定发布物集合的点赞状态
     */
    Map<String, Boolean> listLikeStatus(List<String> postIds, String userId);
}
