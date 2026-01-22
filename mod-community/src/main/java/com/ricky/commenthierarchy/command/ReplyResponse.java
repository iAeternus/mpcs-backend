package com.ricky.commenthierarchy.command;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReplyResponse implements Response {

    String commentId;
    String commentHierarchyId;

}
