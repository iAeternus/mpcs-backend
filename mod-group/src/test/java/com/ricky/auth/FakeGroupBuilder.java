package com.ricky.auth;

import com.ricky.common.permission.Permission;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.InheritancePolicy;
import com.ricky.group.domain.MemberAuthorization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeGroupBuilder {

    private final String id;
    private boolean active = true;
    private InheritancePolicy policy = InheritancePolicy.FULL;

    private final Set<String> members = new HashSet<>();
    private final Set<String> managers = new HashSet<>();
    private final Map<String, Map<String, Set<Permission>>> memberGrants = new HashMap<>();

    private FakeGroupBuilder(String id) {
        this.id = id;
    }

    public static FakeGroupBuilder group(String id) {
        return new FakeGroupBuilder(id);
    }

    public FakeGroupBuilder inactive() {
        this.active = false;
        return this;
    }

    public FakeGroupBuilder fullInheritance() {
        this.policy = InheritancePolicy.FULL;
        return this;
    }

    public FakeGroupBuilder noInheritance() {
        this.policy = InheritancePolicy.NONE;
        return this;
    }

    public FakeGroupBuilder overridable() {
        this.policy = InheritancePolicy.OVERRIDABLE;
        return this;
    }

    public FakeGroupBuilder selective() {
        this.policy = InheritancePolicy.SELECTIVE;
        return this;
    }

    public FakeGroupBuilder member(String userId) {
        members.add(userId);
        return this;
    }

    public FakeGroupBuilder manager(String userId) {
        members.add(userId);
        managers.add(userId);
        return this;
    }

    public FakeGroupBuilder permission(String userId, String folderId, Permission... permissions) {
        memberGrants.computeIfAbsent(userId, key -> new HashMap<>())
                .put(folderId, Set.of(permissions));
        return this;
    }

    public Group build() {
        Group group = mock(Group.class);

        when(group.getId()).thenReturn(id);
        when(group.isActive()).thenReturn(active);
        when(group.containsMember(any()))
                .thenAnswer(inv -> members.contains(inv.getArgument(0)));
        when(group.containsManager(any()))
                .thenAnswer(inv -> managers.contains(inv.getArgument(0)));
        when(group.getMemberAuthorizations()).thenReturn(memberGrants.entrySet().stream()
                .map(entry -> MemberAuthorization.builder()
                        .userId(entry.getKey())
                        .grants(entry.getValue())
                        .inheritancePolicy(policy)
                        .build())
                .toList());

        when(group.appliesTo(any(), any()))
                .thenAnswer(inv -> {
                    String userId = inv.getArgument(0);
                    String folderId = inv.getArgument(1);
                    return resolvedGrants(userId).containsKey(folderId);
                });

        when(group.grantsOf(any()))
                .thenAnswer(inv -> resolvedGrants(inv.getArgument(0)));

        when(group.permissionsOf(any(), any()))
                .thenAnswer(inv -> resolve(resolvedGrants(inv.getArgument(0)), inv.getArgument(1)));

        return group;
    }

    private Map<String, Set<Permission>> resolvedGrants(String userId) {
        return memberGrants.getOrDefault(userId, Map.of());
    }

    private Set<Permission> resolve(Map<String, Set<Permission>> resolved, List<String> ancestors) {
        return switch (policy) {
            case NONE -> resolved.getOrDefault(ancestors.get(0), Set.of());
            case FULL, SELECTIVE -> ancestors.stream()
                    .filter(resolved::containsKey)
                    .flatMap(id -> resolved.get(id).stream())
                    .collect(toImmutableSet());
            case OVERRIDABLE -> {
                for (String ancestorId : ancestors) {
                    if (resolved.containsKey(ancestorId)) {
                        yield resolved.get(ancestorId);
                    }
                }
                yield Set.of();
            }
        };
    }
}
