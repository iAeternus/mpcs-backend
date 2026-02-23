package com.ricky.group.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ricky.common.exception.MyException.accessDeniedException;
import static java.util.Objects.requireNonNull;

// 说明：这里只做“组管理权限”校验（是否为该组管理员），不等同于资源权限系统（文件/授权）。
// 若未来统一权限体系，应抽取统一的角色/权限评估入口，避免管理权限与资源权限逻辑分散。
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
