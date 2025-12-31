package com.ricky.login.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.login.domain.dto.cmd.MobileOrEmailLoginCommand;
import com.ricky.login.domain.dto.cmd.VerificationCodeLoginCommand;
import jakarta.validation.Valid;

public interface LoginService {
    String loginWithMobileOrEmail(MobileOrEmailLoginCommand command);

    String loginWithVerificationCode(VerificationCodeLoginCommand command);

    String refreshToken(UserContext userContext);
}
