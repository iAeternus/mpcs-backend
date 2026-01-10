package com.ricky.group.domain;

import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCachedGroups implements ValueObject {

    List<UserCachedGroup> groups;

}
