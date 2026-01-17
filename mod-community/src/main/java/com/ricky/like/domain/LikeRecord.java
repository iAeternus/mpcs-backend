package com.ricky.like.domain;

import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * 点赞记录
 */
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeRecord implements ValueObject {

    String userId;
    String postId;
    LikeStatus status;

}
