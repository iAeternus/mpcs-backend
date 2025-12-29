package com.ricky.verification.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.verification.domain.dto.cmd.*;
import jakarta.validation.Valid;

public interface VerificationCodeService {
    String createVerificationCodeForRegister(@Valid CreateRegisterVerificationCodeCommand command);

    String createVerificationCodeForLogin(@Valid CreateLoginVerificationCodeCommand command);

    String createVerificationCodeForFindBackPassword(@Valid CreateFindBackPasswordVerificationCodeCommand command);

    String createVerificationCodeForChangeMobile(@Valid CreateChangeMobileVerificationCodeCommand command, UserContext userContext);

    String createVerificationCodeForIdentifyMobile(@Valid IdentifyMobileVerificationCodeCommand command, UserContext userContext);
}
