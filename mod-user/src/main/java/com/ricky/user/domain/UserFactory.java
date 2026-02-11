package com.ricky.user.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.common.domain.SpaceType.personalCustomId;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final FolderFactory folderFactory;

    public CreateUserResult create(String mobile, String email, String password, UserContext userContext) {
        User user = new User(mobile, email, password, userContext);
        Folder root = folderFactory.createRoot(personalCustomId(user.getId()), userContext);
        user.setCustomId(root.getCustomId());
        return CreateUserResult.builder()
                .user(user)
                .root(root)
                .build();
    }

}
