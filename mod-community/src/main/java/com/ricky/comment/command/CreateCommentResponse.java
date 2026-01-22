package com.ricky.comment.command;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCommentResponse implements Response {

    String commentId;
    String commentHierarchyId;

}
