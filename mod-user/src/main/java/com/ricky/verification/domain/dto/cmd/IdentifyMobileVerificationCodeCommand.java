package com.ricky.verification.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.mobile.Mobile;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentifyMobileVerificationCodeCommand implements Command {

    @Mobile
    @NotBlank
    String mobile;

}
