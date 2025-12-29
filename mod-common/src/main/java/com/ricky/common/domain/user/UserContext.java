package com.ricky.common.domain.user;

import com.ricky.common.utils.ValidationUtils;
import lombok.Data;

import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className UserContext
 * @desc 用户上下文，使用ThreadLocal传递
 */
@Data
public class UserContext {

    public static final UserContext NOUSER = new UserContext(null, null, null);
    public static final UserContext ANONYMOUS_USER = NOUSER;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private Role role;

    private UserContext(String uid, String username, Role role) {
        this.uid = uid;
        this.username = username;
        this.role = role;
    }

    public static UserContext of(String uid, String username, Role role) {
        return new UserContext(uid, username, role);
    }

    public boolean isSelf(String uid) {
        requireNotBlank(uid, "uid must not be null");
        return ValidationUtils.equals(uid, this.uid);
    }

    public boolean isHumanUser() {
        if (!internalIsLoggedIn()) {
            return false;
        }

        return internalIsHumanUser();
    }

    public boolean isLoggedIn() {
        return internalIsLoggedIn();
    }

    private boolean internalIsLoggedIn() {
        return isNotBlank(uid) && role != null;
    }

    private boolean internalIsHumanUser() {
        return role == Role.SYS_ADMIN || role == Role.NORMAL_USER;
    }

}
