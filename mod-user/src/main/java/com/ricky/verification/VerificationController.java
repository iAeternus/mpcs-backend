package com.ricky.verification;

import com.ricky.common.domain.dto.resp.IdResponse;
import com.ricky.common.domain.user.UserContext;
import com.ricky.verification.domain.dto.cmd.*;
import com.ricky.verification.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ricky.common.domain.dto.resp.IdResponse.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@Tag(name = "验证码模块")
@RequiredArgsConstructor
@RequestMapping(value = "/verification-codes")
public class VerificationController {

    private final VerificationCodeService verificationCodeService;

    @ResponseStatus(CREATED)
    @Operation(summary = "为注册生成验证码")
    @PostMapping(value = "/for-register")
    public IdResponse createVerificationCodeForRegister(@RequestBody @Valid CreateRegisterVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeService.createVerificationCodeForRegister(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "为登录生成验证码")
    @PostMapping(value = "/for-login")
    public IdResponse createVerificationCodeForLogin(@RequestBody @Valid CreateLoginVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeService.createVerificationCodeForLogin(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "为找回密码生成验证码")
    @PostMapping(value = "/for-find-back-password")
    public IdResponse createVerificationCodeForFindBackPassword(@RequestBody @Valid CreateFindBackPasswordVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeService.createVerificationCodeForFindBackPassword(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "为修改手机号生成验证码")
    @PostMapping(value = "/for-change-mobile")
    public IdResponse createVerificationCodeForChangeMobile(@RequestBody @Valid CreateChangeMobileVerificationCodeCommand command,
                                                            @AuthenticationPrincipal UserContext userContext) {
        String verificationCodeId = verificationCodeService.createVerificationCodeForChangeMobile(command, userContext);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "为手机号认证生成验证码")
    @PostMapping(value = "/for-identify-mobile")
    public IdResponse createVerificationCodeForIdentifyMobile(@RequestBody @Valid IdentifyMobileVerificationCodeCommand command,
                                                              @AuthenticationPrincipal UserContext userContext) {
        String verificationCodeId = verificationCodeService.createVerificationCodeForIdentifyMobile(command, userContext);
        return returnId(verificationCodeId);
    }

}
