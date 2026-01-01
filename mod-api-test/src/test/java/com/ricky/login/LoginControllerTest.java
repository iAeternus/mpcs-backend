package com.ricky.login;

import com.ricky.BaseApiTest;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.login.domain.dto.cmd.MobileOrEmailLoginCommand;
import com.ricky.login.domain.dto.cmd.VerificationCodeLoginCommand;
import com.ricky.user.UserApi;
import com.ricky.user.domain.User;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.user.domain.dto.resp.UserInfoResponse;
import com.ricky.verification.VerificationCodeApi;
import com.ricky.verification.domain.VerificationCode;
import com.ricky.verification.domain.dto.cmd.CreateLoginVerificationCodeCommand;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static com.ricky.RandomTestFixture.*;
import static com.ricky.common.exception.ErrorCodeEnum.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class LoginControllerTest extends BaseApiTest {

    @Test
    public void should_login_with_mobile() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);
        String jwt = LoginApi.loginWithMobileOrEmail(mobile, password);

        UserInfoResponse response = UserApi.myUserInfo(jwt);

        assertEquals(mobile, response.getMobile());
    }

    @Test
    public void should_login_with_email() {
        String email = rEmail();
        String password = rPassword();
        setupApi.register(email, password);
        String jwt = LoginApi.loginWithMobileOrEmail(email, password);

        UserInfoResponse response = UserApi.myUserInfo(jwt);

        assertEquals(email, response.getEmail());
    }

    @Test
    public void should_login_with_verification_code() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());

        String codeId = VerificationCodeApi.createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);

        String jwt = LoginApi.loginWithVerificationCode(VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode()).build());
        UserInfoResponse response = UserApi.myUserInfo(jwt);

        assertEquals(mobile, response.getMobile());
    }

    @Test
    public void should_fail_login_with_non_existing_mobile() {
        var command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(rMobile()).password(rPassword()).build();

        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_login_with_non_existing_email() {
        var command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(rEmail()).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_login_with_wrong_password() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        var command = MobileOrEmailLoginCommand.builder()
                .mobileOrEmail(mobile).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(command), AUTHENTICATION_FAILED);
    }

    @Test
    public void should_fail_verification_login_with_wrong_verification_code() {
        String mobile = rMobile();
        setupApi.register(mobile, rPassword());

        var command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(rVerificationCode()).build();
        assertError(() -> LoginApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
    }

    @Test
    public void should_logout() {
        LoginApi.logout();
    }

    @Test
    public void should_refresh_token() throws InterruptedException {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        String jwt = LoginApi.loginWithMobileOrEmail(mobile, password);
        Thread.sleep(1000);
        String refreshedJwt = LoginApi.refreshToken(jwt);
        UserApi.myUserInfo(refreshedJwt);// validate refreshed token
    }

    @Test
    public void should_failed_login_if_locked() {
        String email = rEmail();
        String password = rPassword();
        var loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(password).build();
        RegisterResponse response = setupApi.register(email, password);
        User user = userRepository.byId(response.getUserId());
        assertNotNull(LoginApi.loginWithMobileOrEmail(loginCommand));

        ReflectionTestUtils.setField(user.getFailedLoginCount(), "count", 51);
        userRepository.save(user);

        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), USER_ALREADY_LOCKED);
    }

    @Test
    public void should_fail_authentication_if_locked() {
        String email = rEmail();
        String password = rPassword();
        var loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(password).build();
        LoginResponse response = setupApi.registerWithLogin(email, password);
        User user = userRepository.byId(response.getUserId());
        assertNotNull(LoginApi.loginWithMobileOrEmail(loginCommand));

        ReflectionTestUtils.setField(user.getFailedLoginCount(), "count", 51);
        userRepository.save(user);
        assertError(() -> UserApi.myProfileRaw(response.getJwt()), USER_ALREADY_LOCKED);
    }

    @Test
    public void should_count_failed_password_login() {
        String email = rEmail();
        String password = rPassword();
        RegisterResponse response = setupApi.register(email, password);
        var loginCommand = MobileOrEmailLoginCommand.builder().mobileOrEmail(email).password(rPassword()).build();
        assertError(() -> LoginApi.loginWithMobileOrEmailRaw(loginCommand), AUTHENTICATION_FAILED);

        User user = userRepository.byId(response.getUserId());
        assertEquals(1, user.getFailedLoginCount().getCount());
    }

}
