package com.ricky.apitest.user;

import com.ricky.apitest.BaseApiTest;
import com.ricky.apitest.verification.VerificationCodeApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.command.UploadAvatarResponse;
import com.ricky.user.domain.User;
import com.ricky.user.domain.event.UserCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static com.ricky.apitest.RandomTestFixture.*;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.event.DomainEventType.USER_CREATED;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

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

        String customId = personalCustomId(user.getId());
        assertTrue(folderRepository.existsRoot(customId));
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

    @Test
    public void should_upload_my_avatar() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/large-file.png");
        java.io.File avatar = resource.getFile();

        UploadAvatarResponse resp = UserApi.uploadAvatar(manager.getJwt(), avatar);

        User user = userRepository.byId(manager.getUserId());
        assertNotNull(resp.getAvatarUrl());
        assertEquals(resp.getAvatarUrl(), user.getAvatarUrl());
        assertTrue(resp.getAvatarUrl().startsWith(manager.getUserId() + "/"));
    }

    @Test
    public void should_fail_to_upload_avatar_if_file_type_unsupported() throws IOException {
        LoginResponse manager = setupApi.registerWithLogin();
        ClassPathResource resource = new ClassPathResource("testdata/plain-text-file.txt");
        java.io.File avatar = resource.getFile();

        assertError(() -> UserApi.uploadAvatarRaw(manager.getJwt(), avatar), UNSUPPORTED_FILE_TYPES);
    }

    @Test
    public void should_fail_to_upload_avatar_if_file_empty() {
        LoginResponse manager = setupApi.registerWithLogin();

        assertError(() -> UserApi.uploadAvatarRaw(manager.getJwt(), "empty.png", new byte[0], "image/png"),
                FILE_MUST_NOT_BE_EMPTY);
    }

}
