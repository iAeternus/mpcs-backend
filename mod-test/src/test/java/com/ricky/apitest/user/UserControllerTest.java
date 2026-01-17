package com.ricky.apitest.user;

import com.ricky.apitest.BaseApiTest;
import com.ricky.folderhierarchy.domain.FolderHierarchy;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.domain.User;
import com.ricky.user.domain.event.UserCreatedEvent;
import com.ricky.apitest.verification.VerificationCodeApi;
import org.junit.jupiter.api.Test;

import static com.ricky.apitest.RandomTestFixture.*;
import static com.ricky.common.event.DomainEventType.USER_CREATED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserControllerTest extends BaseApiTest {

    @Test
    public void should_register_with_mobile() {
        // Given
        String username = rUsername();
        String mobile = rMobile();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        // When
        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        // Then
        User user = userRepository.byId(response.getUserId());
        assertNotNull(user);
        assertEquals(username, user.getUsername());

        FolderHierarchy hierarchy = folderHierarchyDomainService.personalSpaceOf(user.getId());
        assertEquals(user.getId(), hierarchy.getUserId());
    }

    @Test
    public void should_register_with_email() {
        // Given
        String username = rUsername();
        String email = rEmail();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(email);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        // When
        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        // Then
        User user = userRepository.byId(response.getUserId());
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    public void should_fail_to_register_if_mobile_already_exists() {
        // Given
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

        // When & Then
        assertError(() -> UserApi.registerRaw(command), USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }


    @Test
    public void should_fail_to_register_if_email_already_exists() {
        // Given
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

        // When & Then
        assertError(() -> UserApi.registerRaw(command), USER_WITH_MOBILE_OR_EMAIL_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_to_register_if_verification_not_valid() {
        // Given
        String username = rUsername();
        String email = rEmail();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build();

        // When & Then
        assertError(() -> UserApi.registerRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_fail_to_register_if_not_agree_agreement() {
        // Given
        String username = rUsername();
        String email = rEmail();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(email)
                .verification(rVerificationCode())
                .password(rPassword())
                .username(username)
                .agreement(false)
                .build();

        // When & Then
        assertError(() -> UserApi.registerRaw(command), MUST_SIGN_AGREEMENT);
    }

    @Test
    public void should_raise_tenant_created_event_after_register() {
        // Given
        String username = rUsername();
        String mobile = rMobile();

        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobile);
        String verificationCode = verificationCodeRepository.byId(verificationCodeId).getCode();

        // When
        RegisterResponse response = UserApi.register(RegisterCommand.builder()
                .mobileOrEmail(mobile)
                .verification(verificationCode)
                .password(rPassword())
                .username(username)
                .agreement(true)
                .build());

        // Then
        UserCreatedEvent evt = latestEventFor(response.getUserId(), USER_CREATED, UserCreatedEvent.class);
        assertEquals(response.getUserId(), evt.getUserId());
    }

}
