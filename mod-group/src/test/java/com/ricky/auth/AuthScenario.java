package com.ricky.auth;

import com.ricky.common.auth.Permission;
import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.group.domain.Group;
import com.ricky.group.domain.GroupDomainService;
import com.ricky.group.domain.GroupRepository;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthScenario {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final FolderHierarchyDomainService hierarchyService;

    private final List<Group> groups = new ArrayList<>();
    private User user;

    private final GroupDomainService service;
    private Set<Permission> resolved;

    AuthScenario(GroupRepository gr, UserRepository ur, FolderHierarchyDomainService hs) {
        this.groupRepository = gr;
        this.userRepository = ur;
        this.hierarchyService = hs;

        this.service = new GroupDomainService(gr, ur, hs);
    }

    public AuthScenario givenUser(String userId, String... groupIds) {
        user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getGroupIds()).thenReturn(Set.of(groupIds));
        when(userRepository.cachedById(userId)).thenReturn(user);
        return this;
    }

    public AuthScenario givenGroup(FakeGroupBuilder builder) {
        Group g = builder.build();
        groups.add(g);
        return this;
    }

    public AuthScenario givenHierarchy(String customId, FolderPath path) {
        when(hierarchyService.withAllParentIdsOf(customId, path.currFolderId()))
                .thenReturn(path.ancestors());
        return this;
    }


    public AuthScenario whenResolve(String userId, String customId, String folderId) {
        when(groupRepository.byIds(user.getGroupIds()))
                .thenReturn(groups);

        resolved = service.resolvePermissions(userId, customId, folderId);
        return this;
    }

    public AuthScenario thenPermissionsAre(Permission... expected) {
        assertThat(resolved)
                .containsExactlyInAnyOrder(expected);
        return this;
    }

    public AuthScenario thenHas(Permission permission) {
        assertThat(resolved).contains(permission);
        return this;
    }

    public AuthScenario thenEmpty() {
        assertThat(resolved).isEmpty();
        return this;
    }

    public record FolderPath(List<String> nodes) {

        // path("parent", "child") -> parent/child
        public static FolderPath path(String... nodes) {
            return new FolderPath(List.of(nodes));
        }

        public String currFolderId() {
            return nodes.get(nodes.size() - 1);
        }

        // 逆序
        public List<String> ancestors() {
            return IntStream.range(0, nodes.size())
                    .map(i -> nodes.size() - 1 - i)
                    .mapToObj(nodes::get)
                    .collect(toImmutableList());
        }
    }

}
