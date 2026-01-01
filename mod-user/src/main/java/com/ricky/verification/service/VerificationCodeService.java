package com.ricky.verification.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.verification.domain.dto.cmd.*;

public interface VerificationCodeService {
    String createVerificationCodeForRegister(CreateRegisterVerificationCodeCommand command);

    String createVerificationCodeForLogin(CreateLoginVerificationCodeCommand command);

    String createVerificationCodeForFindBackPassword(CreateFindBackPasswordVerificationCodeCommand command);

    String createVerificationCodeForChangeMobile(CreateChangeMobileVerificationCodeCommand command, UserContext userContext);

    String createVerificationCodeForIdentifyMobile(IdentifyMobileVerificationCodeCommand command, UserContext userContext);
}
