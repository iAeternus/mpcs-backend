package com.ricky.group.domain;

import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member implements ValueObject {

    String userId;
    MemberRole role;
    Instant joinedAt;

}
