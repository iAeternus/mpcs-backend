package com.ricky.comment.domain;

import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentCount implements ValueObject {

    String postId;
    Integer commentCount;

}
