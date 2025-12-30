package com.ricky.login.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.mobileoremail.MobileOrEmail;
import com.ricky.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VerificationCodeLoginCommand implements Command {

    @NotBlank
    @MobileOrEmail
    String mobileOrEmail;

    @NotBlank
    @VerificationCode
    String verification;

}
