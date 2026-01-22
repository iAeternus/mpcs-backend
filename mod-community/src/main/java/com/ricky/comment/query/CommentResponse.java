package com.ricky.comment.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse implements Response {

    String id;
    String userId; // 用户ID
    String postId; // 发布物ID
    String content;
    LocalDateTime createdTime;

}
