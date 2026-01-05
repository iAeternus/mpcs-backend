package com.ricky.common.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ricky.common.utils.ValidationUtils;
import lombok.Value;

import static com.ricky.common.utils.ValidationUtils.requireNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Ricky
 * @version 1.0
 * @date 2025/2/26
 * @className UserContext
 * @desc 用户上下文，使用ThreadLocal传递
 */
@Value
public class UserContext {

    public static final UserContext NOUSER = new UserContext(null, null, null);
    public static final UserContext ANONYMOUS_USER = NOUSER;

    /**
     * 用户ID
     */
    String uid;

    /**
     * 用户名
     */
    String username;

    /**
     * 角色
     */
    Role role;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public UserContext(
            @JsonProperty("uid") String uid,
            @JsonProperty("username") String username,
            @JsonProperty("role") Role role) {
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
