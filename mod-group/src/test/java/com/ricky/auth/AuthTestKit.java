package com.ricky.auth;

import com.ricky.folderhierarchy.domain.FolderHierarchyDomainService;
import com.ricky.group.domain.GroupRepository;
import com.ricky.user.domain.UserRepository;

import static org.mockito.Mockito.mock;

public final class AuthTestKit {

    private AuthTestKit() {
    }

    public static AuthScenario scenario() {
        return new AuthScenario(
                mock(GroupRepository.class),
                mock(UserRepository.class),
                mock(FolderHierarchyDomainService.class)
        );
    }

}
