package com.ricky.comment.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentResponse implements Response {

    @Field("_id")
    String commentId;
    String parentId;
    String username; // 用户名
    String postId; // 发布物ID
    String content;
    Instant createdAt;

}
