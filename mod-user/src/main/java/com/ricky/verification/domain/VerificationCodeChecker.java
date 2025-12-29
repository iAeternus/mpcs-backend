package com.ricky.verification.domain;

import com.ricky.common.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.ricky.common.exception.ErrorCodeEnum.VERIFICATION_CODE_CHECK_FAILED;
import static com.ricky.common.utils.CommonUtils.maskMobileOrEmail;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class VerificationCodeChecker {

    private final VerificationCodeRepository verificationCodeRepository;

    // REQUIRES_NEW表示无论最终结果成败，只要check了一次，便标记使用一次
    @Transactional(propagation = REQUIRES_NEW)
    public void check(String mobileOrEmail, String code, VerificationCodeType type) {
        VerificationCode verificationCode = verificationCodeRepository.findValidOptional(mobileOrEmail, code, type)
                .orElseThrow(() -> new MyException(VERIFICATION_CODE_CHECK_FAILED, "验证码验证失败。",
                        "mobileOrEmail", maskMobileOrEmail(mobileOrEmail)));
        verificationCode.use();
        verificationCodeRepository.save(verificationCode);
    }

}
