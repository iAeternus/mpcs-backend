package com.ricky.auth;

import com.ricky.common.permission.Permission;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.InheritancePolicy;

import java.util.*;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FakeGroupBuilder {

    private final String id;
    private boolean active = true;
    private InheritancePolicy policy = InheritancePolicy.FULL;

    private final Set<String> members = new HashSet<>();
    private final Map<String, Set<Permission>> grants = new HashMap<>();

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

    public FakeGroupBuilder permission(String folderId, Permission... permissions) {
        grants.put(folderId, Set.of(permissions));
        return this;
    }

    public Group build() {
        Group g = mock(Group.class);

        when(g.getId()).thenReturn(id);
        when(g.isActive()).thenReturn(active);

        when(g.containsMember(any()))
                .thenAnswer(inv -> members.contains(inv.getArgument(0)));

        when(g.appliesTo(any()))
                .thenReturn(true);

        when(g.permissionsOf(any()))
                .thenAnswer(inv -> {
                    List<String> ancestors = inv.getArgument(0);

                    return switch (policy) {
                        case NONE -> grants.getOrDefault(ancestors.get(0), Set.of());
                        // SELECTIVE暂时与FULL坐一桌
                        case FULL, SELECTIVE -> ancestors.stream()
                                .filter(grants::containsKey)
                                .flatMap(id -> grants.get(id).stream())
                                .collect(toImmutableSet());
                        case OVERRIDABLE -> {
                            for (String id : ancestors) {
                                if (grants.containsKey(id)) {
                                    yield grants.get(id);
                                }
                            }
                            yield Set.of();
                        }
                    };
                });

        return g;
    }

}
