package com.ricky.verification.domain;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.ricky.common.exception.ErrorCodeEnum.TOO_MANY_VERIFICATION_CODE_FOR_TODAY;
import static com.ricky.common.exception.ErrorCodeEnum.VERIFICATION_CODE_ALREADY_SENT;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeFactory {

    private final VerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;

    public Optional<VerificationCode> create(String mobileOrEmail, VerificationCodeType type, String userId, UserContext userContext) {
        try {
            if (verificationCodeRepository.existsWithinOneMinutes(mobileOrEmail, type)) {
                throw new MyException(VERIFICATION_CODE_ALREADY_SENT, "1分钟内只能获取一次验证码。",
                        "mobileOrEmail", maskMobileOrEmail(mobileOrEmail));
            }

            if (verificationCodeRepository.totalCodeCountOfTodayFor(mobileOrEmail) > 20) {
                throw new MyException(TOO_MANY_VERIFICATION_CODE_FOR_TODAY, "验证码获取次数超过当天限制。",
                        "mobileOrEmail", maskMobileOrEmail(mobileOrEmail));
            }

//            if (isNotBlank(userId) && isMobileNumber(mobileOrEmail)) {
//                PackagesStatus packagesStatus = userRepository.packagesStatusOf(userId);
//                if (packagesStatus.isMaxSmsCountReached()) {
//                    log.warn("Failed to create verification code for [{}] as SMS count reached max amount for current month for tenant[{}].",
//                            maskMobileOrEmail(mobileOrEmail), userId);
//                    return Optional.empty();
//                }
//            }

            return Optional.of(new VerificationCode(mobileOrEmail, type, userId, userContext));
        } catch (MyException ex) {
            log.warn("Error while create verification code: {}.", ex.getMessage());
            return Optional.empty();
        }
    }
}
