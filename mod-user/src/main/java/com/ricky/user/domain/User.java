package com.ricky.user.domain;

import com.ricky.common.domain.AggregateRoot;
import com.ricky.common.domain.user.Role;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.utils.SnowflakeIdGenerator;
import com.ricky.user.domain.evt.UserCreatedEvent;
import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

import static com.ricky.common.constants.ConfigConstants.USER_COLLECTION;
import static com.ricky.common.constants.ConfigConstants.USER_ID_PREFIX;
import static com.ricky.common.exception.ErrorCodeEnum.USER_ALREADY_LOCKED;
import static java.time.LocalDate.now;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @brief 用户
 */
@Getter
@Document(USER_COLLECTION)
@TypeAlias(USER_COLLECTION)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class User extends AggregateRoot {

    private String username;
    private String mobile; // 手机号，全局唯一，与email不能同时为空
    private String email; // 邮箱，全局唯一，与mobile不能同时为空
    private String password;
    private String avatarUrl;
    private Role role;
    private boolean mobileIdentified; // 是否已验证手机号
    private FailedLoginCount failedLoginCount; // 登录失败次数

    private User(String mobile, String email, String password, UserContext userContext) {
        super(userContext.getUid(), userContext);
        this.username = userContext.getUsername();
        this.role = Role.NORMAL_USER;
        this.mobile = mobile;
        if (isNotBlank(this.mobile)) {
            this.mobileIdentified = true;
        }
        this.email = email;
        this.password = password;
        this.failedLoginCount = FailedLoginCount.init();
        this.addOpsLog("注册", userContext);
    }

    public static User create(String mobile, String email, String password, UserContext userContext) {
        User user = new User(mobile, email, password, userContext);
        user.raiseEvent(new UserCreatedEvent(user.getId(), userContext));
        return user;
    }

    public static String newUserId() {
        return USER_ID_PREFIX + SnowflakeIdGenerator.newSnowflakeId();
    }

    public void checkActive() {
        if (this.failedLoginCount.isLocked()) {
            throw new MyException(USER_ALREADY_LOCKED, "当前用户已经被锁定，次日零点系统将自动解锁。", "userId", this.getId());
        }
    }

    public void useSms() {
        // TODO
    }

    public void recordFailedLogin() {
        this.failedLoginCount.recordFailedLogin();
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class FailedLoginCount {
        private static final int MAX_ALLOWED_FAILED_LOGIN_PER_DAY = 30;

        private LocalDate date;
        private int count;

        public static FailedLoginCount init() {
            return FailedLoginCount.builder().date(LocalDate.now()).count(0).build();
        }

        private void recordFailedLogin() {
            LocalDate now = LocalDate.now();
            if (now.equals(date)) {
                count++;
            } else {
                this.date = now;
                this.count = 0;
            }
        }

        private boolean isLocked() {
            return now().equals(date) && this.count >= MAX_ALLOWED_FAILED_LOGIN_PER_DAY;
        }
    }
}
