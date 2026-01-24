package com.ricky.group.domain;

import com.ricky.common.auth.Permission;
import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CachedGroup implements ValueObject {

    String name;
    boolean active;
    Set<String> managers;
    Set<String> members;
    Map<String, Set<Permission>> grants;
    InheritancePolicy inheritancePolicy;

}
