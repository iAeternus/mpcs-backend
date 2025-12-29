package com.ricky.verification.infra;

import com.ricky.common.profile.NonProdProfile;
import com.ricky.user.domain.task.UserSmsUsageCountTask;
import com.ricky.verification.domain.VerificationCode;
import com.ricky.verification.domain.VerificationCodeSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ricky.common.utils.CommonUtils.isMobileNumber;
import static com.ricky.common.utils.ValidationUtils.isNotBlank;

@Slf4j
@Component
@NonProdProfile
@RequiredArgsConstructor
public class FakeVerificationCodeSender implements VerificationCodeSender {
    private final UserSmsUsageCountTask userSmsUsageCountTask;

    @Override
    public void send(VerificationCode code) {
        String mobileOrEmail = code.getMobileOrEmail();
        String userId = code.getUserId();

        if (isMobileNumber(mobileOrEmail) && isNotBlank(userId)) {
            userSmsUsageCountTask.run(userId);
        }

        log.info("Verification code for {} is {}", mobileOrEmail, code.getCode());
    }
}
