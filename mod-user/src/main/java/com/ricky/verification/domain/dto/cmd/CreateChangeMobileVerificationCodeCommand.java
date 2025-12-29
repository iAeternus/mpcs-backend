package com.ricky.verification.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.validation.mobile.Mobile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateChangeMobileVerificationCodeCommand implements Command {

    @Mobile
    @NotBlank
    @Size(max = 11)
    String mobile;

}
