package com.ricky.publicfile.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicFileResponse implements Response {

    String originalFileId;
    String publisher;
    String title;
    String description;
    Integer likeCount;
    Integer commentCount;
    Instant createdAt;

}
