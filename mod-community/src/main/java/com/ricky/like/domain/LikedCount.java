package com.ricky.like.domain;

import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * 发布物被点赞数
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikedCount implements ValueObject {

    String postId;
    Integer count;

}
