package com.ricky.user.service.impl;

import com.ricky.common.domain.user.Role;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserDomainService;
import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.user.domain.UserRepository;
import com.ricky.user.service.UserService;
import com.ricky.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ricky.verification.domain.VerificationCodeType.REGISTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RateLimiter rateLimiter;
    private final VerificationCodeChecker verificationCodeChecker;
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RegisterResponse register(RegisterCommand command) {
        rateLimiter.applyFor("Registration:Register:All", 20);

        String mobileOrEmail = command.getMobileOrEmail();
        verificationCodeChecker.check(mobileOrEmail, command.getVerification(), REGISTER);

        UserContext userContext = UserContext.of(User.newUserId(), command.getUsername(), Role.NORMAL_USER);
        User user = userDomainService.register(
                mobileOrEmail,
                command.getPassword(),
                command.getUsername(),
                userContext);

        userRepository.save(user);
        log.info("Registered user[{}]", user.getId());

        return RegisterResponse.builder().userId(user.getId()).build();
    }
}
