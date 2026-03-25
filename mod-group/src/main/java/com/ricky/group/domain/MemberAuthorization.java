package com.ricky.group.domain;

import com.ricky.common.domain.marker.ValueObject;
import com.ricky.common.permission.Permission;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberAuthorization implements ValueObject {

    String userId;
    @Builder.Default
    Map<String, Set<Permission>> grants = new HashMap<>();
    @Builder.Default
    InheritancePolicy inheritancePolicy = InheritancePolicy.NONE;

    public MemberAuthorization grant(String folderId, Set<Permission> permissions) {
        Map<String, Set<Permission>> nextGrants = copyGrants();
        nextGrants.put(folderId, new HashSet<>(permissions));
        return toBuilder()
                .grants(nextGrants)
                .build();
    }

    public MemberAuthorization grant(List<String> folderIds, Set<Permission> permissions) {
        Map<String, Set<Permission>> nextGrants = copyGrants();
        for (String folderId : folderIds) {
            nextGrants.put(folderId, new HashSet<>(permissions));
        }
        return toBuilder()
                .grants(nextGrants)
                .build();
    }

    public boolean appliesTo(String folderId) {
        return grants != null && grants.containsKey(folderId);
    }

    public Set<Permission> permissionsOf(List<String> ancestors) {
        return PermissionInheritanceResolver.resolve(inheritancePolicy, grants, ancestors);
    }

    private Map<String, Set<Permission>> copyGrants() {
        Map<String, Set<Permission>> copied = new HashMap<>();
        if (grants == null) {
            return copied;
        }
        grants.forEach((folderId, permissions) -> copied.put(folderId, new HashSet<>(permissions)));
        return copied;
    }
}
