package com.ricky.group.domain;

import com.ricky.common.permission.Permission;
import com.ricky.common.domain.marker.ValueObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCachedGroup implements ValueObject {

    String id;
    String name;
    boolean active;
    String userId;
    List<String> managers;
    List<String> members;
    Map<String, Set<Permission>> grants;
    InheritancePolicy inheritancePolicy;

    public boolean isVisible() {
        return active;
    }

    public boolean containsManager(String managerId) {
        return managers.contains(managerId);
    }

    public boolean containsMember(String memberId) {
        return members.contains(memberId);
    }

}
