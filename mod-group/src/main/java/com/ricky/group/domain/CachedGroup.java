package com.ricky.group.domain;

import com.ricky.common.domain.marker.ValueObject;
import com.ricky.common.permission.Permission;
import com.ricky.common.utils.ValidationUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CachedGroup implements ValueObject {

    String name;
    boolean active;
    List<Member> members;
    Map<String, Set<Permission>> grants;
    InheritancePolicy inheritancePolicy;

    public Set<String> managerIds() {
        if (ValidationUtils.isEmpty(members)) {
            return Set.of();
        }
        return members.stream()
                .filter(member -> member.getRole() == MemberRole.ADMIN)
                .map(Member::getUserId)
                .collect(toImmutableSet());
    }

    public Set<String> memberIds() {
        if (ValidationUtils.isEmpty(members)) {
            return Set.of();
        }
        return members.stream()
                .map(Member::getUserId)
                .collect(toImmutableSet());
    }
}
