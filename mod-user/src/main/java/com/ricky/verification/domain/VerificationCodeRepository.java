package com.ricky.verification.domain;

import java.util.Optional;

public interface VerificationCodeRepository {
    Optional<VerificationCode> findValidOptional(String mobileOrEmail, String code, VerificationCodeType type);

    void save(VerificationCode verificationCode);

    VerificationCode byId(String verificationCodeId);

    boolean existsWithinOneMinutes(String mobileOrEmail, VerificationCodeType type);

    long totalCodeCountOfTodayFor(String mobileOrEmail);

    boolean exists(String arId);
}
