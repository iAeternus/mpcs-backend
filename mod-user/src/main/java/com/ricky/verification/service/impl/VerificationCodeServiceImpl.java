package com.ricky.verification.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.user.domain.UserRepository;
import com.ricky.verification.domain.*;
import com.ricky.verification.domain.dto.cmd.*;
import com.ricky.verification.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.ricky.common.domain.user.UserContext.NOUSER;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;
import static com.ricky.verification.domain.VerificationCode.newVerificationCodeId;
import static com.ricky.verification.domain.VerificationCodeType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private final RateLimiter rateLimiter;
    private final UserRepository userRepository;
    private final VerificationCodeFactory verificationCodeFactory;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeSender verificationCodeSender;
    
    @Transactional
    public String createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        rateLimiter.applyFor("VerificationCode:Register:All", 20);
        rateLimiter.applyFor("VerificationCode:Register:" + mobileOrEmail, 1);

        if (userRepository.existsByMobileOrEmail(mobileOrEmail)) {
            log.warn("[{}] already exists for register.", maskMobileOrEmail(mobileOrEmail));
            return newVerificationCodeId();
        }

        String verificationCodeId = createVerificationCode(mobileOrEmail, REGISTER, null, NOUSER);
        log.info("Created verification code[{}] for register for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForLogin(CreateLoginVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        rateLimiter.applyFor("VerificationCode:Login:All", 100);
        rateLimiter.applyFor("VerificationCode:Login:" + mobileOrEmail, 1);

        String verificationCodeId = userRepository.byMobileOrEmailOptional(mobileOrEmail)
                .map(user -> createVerificationCode(mobileOrEmail, LOGIN, user.getId(), NOUSER))
                .orElseGet(() -> {
                    log.warn("No user exists for [{}] for login.", maskMobileOrEmail(mobileOrEmail));
                    return newVerificationCodeId();
                });

        log.info("Created verification code[{}] for login for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForFindBackPassword(CreateFindBackPasswordVerificationCodeCommand command) {
        String mobileOrEmail = command.getMobileOrEmail();
        rateLimiter.applyFor("VerificationCode:FindBackPassword:All", 10);
        rateLimiter.applyFor("VerificationCode:FindBackPassword:" + mobileOrEmail, 1);

        String verificationCodeId = userRepository.byMobileOrEmailOptional(mobileOrEmail)
                .map(user -> createVerificationCode(mobileOrEmail, FIND_BACK_PASSWORD, user.getId(), NOUSER))
                .orElseGet(() -> {
                    log.warn("No user exists for [{}] for find back password.", mobileOrEmail);
                    return newVerificationCodeId();
                });

        log.info("Created verification code[{}] for find back password for [{}].",
                verificationCodeId, maskMobileOrEmail(command.getMobileOrEmail()));
        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForChangeMobile(CreateChangeMobileVerificationCodeCommand command, UserContext userContext) {
        String mobile = command.getMobile();
        rateLimiter.applyFor("VerificationCode:ChangeMobile:All", 10);
        rateLimiter.applyFor("VerificationCode:ChangeMobile:" + mobile, 1);

        if (userRepository.existsByMobile(mobile)) {
            log.warn("Mobile [{}] already exists for change mobile.", maskMobileOrEmail(mobile));
            return newVerificationCodeId();
        }

        String verificationCodeId = createVerificationCode(mobile, CHANGE_MOBILE, userContext.getUid(), userContext);
        log.info("Created verification code[{}] for change mobile for [{}].", verificationCodeId, maskMobileOrEmail(command.getMobile()));

        return verificationCodeId;
    }

    @Transactional
    public String createVerificationCodeForIdentifyMobile(IdentifyMobileVerificationCodeCommand command, UserContext userContext) {
        rateLimiter.applyFor("VerificationCode:IdentifyMobile:All", 20);
        rateLimiter.applyFor("VerificationCode:IdentifyMobile:" + command.getMobile(), 1);

        String verificationCodeId = createVerificationCode(command.getMobile(), IDENTIFY_MOBILE, userContext.getUid(), userContext);
        log.info("Created verification code[{}] for identify mobile for [{}].", verificationCodeId, command.getMobile());
        return verificationCodeId;
    }

    private String createVerificationCode(String mobileOrEmail, VerificationCodeType type, String userId, UserContext userContext) {
        Optional<VerificationCode> verificationCodeOptional = verificationCodeFactory.create(mobileOrEmail, type, userId, userContext);
        if (verificationCodeOptional.isPresent()) {
            VerificationCode code = verificationCodeOptional.get();
            verificationCodeRepository.save(code);
            verificationCodeSender.send(code);
            return code.getId();
        } else {
            return newVerificationCodeId();
        }
    }
}
