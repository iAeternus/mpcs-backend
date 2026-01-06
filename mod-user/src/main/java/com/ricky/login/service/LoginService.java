package com.ricky.login.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.login.command.MobileOrEmailLoginCommand;
import com.ricky.login.command.VerificationCodeLoginCommand;

public interface LoginService {
    String loginWithMobileOrEmail(MobileOrEmailLoginCommand command);

    String loginWithVerificationCode(VerificationCodeLoginCommand command);

    String refreshToken(UserContext userContext);
}
