package com.ricky.comment.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyCommentResponse implements Response {

    String postId; // 发布物ID
    String content;
    Instant createdAt;

}
