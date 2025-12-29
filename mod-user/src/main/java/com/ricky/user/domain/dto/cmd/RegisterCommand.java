package com.ricky.user.domain.dto.cmd;

import com.ricky.common.domain.marker.Command;
import com.ricky.common.exception.MyException;
import com.ricky.common.validation.mobileoremail.MobileOrEmail;
import com.ricky.common.validation.password.Password;
import com.ricky.common.validation.verficationcode.VerificationCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.ricky.common.constants.ConfigConstants.MAX_GENERIC_NAME_LENGTH;
import static com.ricky.common.exception.ErrorCodeEnum.MUST_SIGN_AGREEMENT;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegisterCommand implements Command {

    @NotBlank
    @MobileOrEmail
    String mobileOrEmail;

    @NotBlank
    @VerificationCode
    String verification;

    @NotBlank
    @Password
    String password;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    String username;

    boolean agreement;

    @Override
    public void correctAndValidate() {
        if (!agreement) {
            throw new MyException(MUST_SIGN_AGREEMENT, "请同意用户协议以完成注册。");
        }
    }

}
