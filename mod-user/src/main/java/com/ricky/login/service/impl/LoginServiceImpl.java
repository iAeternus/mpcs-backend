package com.ricky.login.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.common.security.jwt.JwtService;
import com.ricky.login.command.MobileOrEmailLoginCommand;
import com.ricky.login.command.VerificationCodeLoginCommand;
import com.ricky.login.domain.LoginDomainService;
import com.ricky.login.service.LoginService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ricky.common.exception.MyException.authenticationException;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final RateLimiter rateLimiter;
    private final LoginDomainService loginDomainService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public String loginWithMobileOrEmail(MobileOrEmailLoginCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        rateLimiter.applyFor("Login:MobileOrEmail:" + mobileOrEmail, 1);

        try {
            String token = loginDomainService.loginWithMobileOrEmail(mobileOrEmail, command.getPassword());
            log.info("User[{}] logged in using password.", maskMobileOrEmail(command.getMobileOrEmail()));
            return token;
        } catch (Throwable t) {
            if (t instanceof MyException ex && (ex.getCode().getStatus() == 401 || ex.getCode().getStatus() == 409)) {
                throw ex;
            }

            throw authenticationException("手机号或密码登录失败", Map.of("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }
    }

    @Override
    public String loginWithVerificationCode(VerificationCodeLoginCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();

        rateLimiter.applyFor("Login:MobileOrEmail:" + mobileOrEmail, 1);

        try {
            String token = loginDomainService.loginWithVerificationCode(
                    mobileOrEmail,
                    command.getVerification());
            log.info("User[{}] logged in using verification code.", maskMobileOrEmail(command.getMobileOrEmail()));
            return token;
        } catch (Throwable t) {
            if (t instanceof MyException ex && (ex.getCode().getStatus() == 401 || ex.getCode().getStatus() == 409)) {
                throw ex;
            }

            throw authenticationException("验证码登录失败", Map.of("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }
    }

    @Override
    public String refreshToken(UserContext userContext) {
        rateLimiter.applyFor("Login:RefreshToken:All", 1000);

        User user = userRepository.cachedById(userContext.getUid());
        log.info("User[{}] refreshed token.", user.getId());
        return jwtService.generateJwt(user.getId());
    }
}
