package com.ricky.user.domain;

import com.ricky.common.domain.user.UserContext;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User create(String username, String mobile, String email, String password, UserContext userContext) {
        return new User(mobile, email, password, userContext);
    }

}
