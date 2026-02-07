package com.ricky.group.domain;

import com.ricky.common.permission.Permission;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * 权限继承处理
 */
public class PermissionInheritanceResolver {

    public static Set<Permission> resolve(InheritancePolicy policy, Map<String, Set<Permission>> grants, List<String> ancestors) {
        return switch (policy) {
            case NONE -> fromSelfOnly(grants, ancestors);
            case FULL -> fullInheritance(grants, ancestors);
            case SELECTIVE -> selectiveInheritance(grants, ancestors);
            case OVERRIDABLE -> overridableInheritance(grants, ancestors);
        };
    }

    private static Set<Permission> fromSelfOnly(Map<String, Set<Permission>> grants, List<String> ancestors) {
        return grants.getOrDefault(ancestors.get(0), Set.of());
    }

    private static Set<Permission> fullInheritance(Map<String, Set<Permission>> grants, List<String> ancestors) {
        return ancestors.stream()
                .flatMap(id -> grants.getOrDefault(id, Set.of()).stream())
                .collect(toImmutableSet());
    }

    // 目前行为与fullInheritance一致
    private static Set<Permission> selectiveInheritance(Map<String, Set<Permission>> grants, List<String> ancestors) {
        // self，全部
        Set<Permission> result = new HashSet<>(grants.getOrDefault(ancestors.get(0), Set.of()));

        // ancestors，可继承
        ancestors.stream()
                .skip(1)
                .forEach(id -> {
                    Set<Permission> permissions = grants.getOrDefault(id, Set.of()); // 未来在这里过滤出可继承的权限
                    result.addAll(permissions);
                });

        return result;
    }

    private static Set<Permission> overridableInheritance(Map<String, Set<Permission>> grants, List<String> ancestors) {
        for (String id : ancestors) {
            Set<Permission> perms = grants.get(id);
            if (perms != null && !perms.isEmpty()) {
                return perms;
            }
        }
        return Set.of();
    }
}
