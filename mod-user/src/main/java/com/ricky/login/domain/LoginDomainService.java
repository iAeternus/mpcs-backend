package com.ricky.login.domain;

import com.ricky.common.password.IPasswordEncoder;
import com.ricky.common.security.jwt.JwtService;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserDomainService;
import com.ricky.user.domain.UserRepository;
import com.ricky.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ricky.common.exception.MyException.authenticationException;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;
import static com.ricky.verification.domain.VerificationCodeType.LOGIN;

@Service
@RequiredArgsConstructor
public class LoginDomainService {

    private final UserRepository userRepository;
    private final IPasswordEncoder passwordEncoder;
    private final UserDomainService userDomainService;
    private final JwtService jwtService;
    private final VerificationCodeChecker verificationCodeChecker;

    public String loginWithMobileOrEmail(String mobileOrEmail,
                                         String password) {
        User user = userRepository.byMobileOrEmailOptional(mobileOrEmail)
                .orElseThrow(() -> authenticationException("手机号或邮箱登录失败", Map.of("mobileOrEmail", maskMobileOrEmail(mobileOrEmail))));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            userDomainService.recordUserFailedLogin(user);
            throw authenticationException("手机号或邮箱登录失败", Map.of("mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        }

        user.checkActive();
        return jwtService.generateJwt(user.getId());
    }

    public String loginWithVerificationCode(String mobileOrEmail,
                                            String verificationCode) {
        verificationCodeChecker.check(mobileOrEmail, verificationCode, LOGIN);
        User user = userRepository.byMobileOrEmailOptional(mobileOrEmail)
                .orElseThrow(() -> authenticationException("验证码登录失败", Map.of("mobileOrEmail", maskMobileOrEmail(mobileOrEmail))));

        user.checkActive();
        return jwtService.generateJwt(user.getId());
    }

}
