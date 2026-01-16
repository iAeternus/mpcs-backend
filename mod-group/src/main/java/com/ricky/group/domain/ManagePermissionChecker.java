package com.ricky.group.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.common.exception.MyException.accessDeniedException;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class ManagePermissionChecker {

    private final UserRepository userRepository;

    public boolean canManageGroup(Group group, UserContext userContext) {
        requireNonNull(group, "Group must not be null.");
        requireNonNull(userContext, "UserContext must not be null.");

        User user = userRepository.cachedById(userContext.getUid());
        return user.containsGroup(group.getId()) && group.containsManager(userContext.getUid());
    }

    public void checkCanManageGroup(Group group, UserContext userContext) {
        requireNonNull(group, "Group must not be null.");
        requireNonNull(userContext, "UserContext must not be null.");

        if (!canManageGroup(group, userContext)) {
            throw accessDeniedException();
        }
    }

}
