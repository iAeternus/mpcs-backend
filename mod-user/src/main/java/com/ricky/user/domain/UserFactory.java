package com.ricky.user.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public CreateUserResult create(String username, String mobile, String email, String password, UserContext userContext) {
        User user = User.create(mobile, email, password, userContext);
        FolderHierarchy hierarchy = FolderHierarchy.create(userContext);
        return CreateUserResult.builder()
                .user(user)
                .folderHierarchy(hierarchy)
                .build();
    }

}
