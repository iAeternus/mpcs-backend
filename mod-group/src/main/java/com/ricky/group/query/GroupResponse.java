package com.ricky.group.query;

import com.ricky.common.domain.marker.Response;
import com.ricky.group.domain.InheritancePolicy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupResponse implements Response {

    String groupId;
    String customId;
    String name;
    Boolean active;
    InheritancePolicy inheritancePolicy;
    Instant createdAt;
    Instant updatedAt;

}
