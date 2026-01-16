package com.ricky.user.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.folderhierarchy.domain.FolderHierarchyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final FolderHierarchyFactory folderHierarchyFactory;

    public CreateUserResult create(String mobile, String email, String password, UserContext userContext) {
        User user = new User(mobile, email, password, userContext);
        FolderHierarchy hierarchy = folderHierarchyFactory.createPersonalSpace(userContext);
        return CreateUserResult.builder()
                .user(user)
                .folderHierarchy(hierarchy)
                .build();
    }

}
