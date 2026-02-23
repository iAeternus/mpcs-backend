package com.ricky.publicfile.query;

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
public class PublicFileResponse implements Response {

    @Field("_id")
    String postId;
    String originalFileId;
    String publisher;
    String title;
    String description;
    Integer likeCount;
    Integer commentCount;
    Instant createdAt;

}
