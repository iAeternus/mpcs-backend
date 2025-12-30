package com.ricky.user;

import com.ricky.BaseApiTest;
import com.ricky.user.domain.User;
import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.user.domain.evt.UserCreatedEvent;
import com.ricky.verification.VerificationCodeApi;
import org.junit.jupiter.api.Test;

import static com.ricky.RandomTestFixture.*;
import static com.ricky.common.event.DomainEventType.USER_CREATED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest extends BaseApiTest {

    @Test
    public void should_register_with_mobile() {
        String username = rUsername();
        String mobile = rMobile();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        User user = userRepository.byId(response.getUserId());
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    public void should_register_with_email() {
        String username = rUsername();
        String email = rEmail();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(email);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        User user = userRepository.byId(response.getUserId());
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    public void should_fail_to_register_if_mobile_already_exists() {
        String username = rUsername();
        String mobile = rMobile();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build();

        UserApi.register(command); // 先注册以占用手机号
        assertError(() -> UserApi.registerRaw(command), USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }


    @Test
    public void should_fail_to_register_if_email_already_exists() {
        String username = rUsername();
        String email = rEmail();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(email);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build();

        UserApi.register(command); // 先注册以占用邮箱
        assertError(() -> UserApi.registerRaw(command), USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_to_register_if_verification_not_valid() {
        String username = rUsername();
        String email = rEmail();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build();

        assertError(() -> UserApi.registerRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_fail_to_register_if_not_agree_agreement() {
        String username = rUsername();
        String email = rEmail();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .agreement(false)
                .build();

        assertError(() -> UserApi.registerRaw(command), MUST_SIGN_AGREEMENT);
    }

    @Test
    public void should_raise_tenant_created_event_after_register() {
        String username = rUsername();
        String mobile = rMobile();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        UserCreatedEvent evt = latestEventFor(response.getUserId(), USER_CREATED, UserCreatedEvent.class);
        assertEquals(response.getUserId(), evt.getUserId());
    }

}
