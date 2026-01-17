package com.ricky.apitest.verification;

import com.ricky.apitest.BaseApiTest;
import com.ricky.user.command.RegisterResponse;
import com.ricky.verification.domain.VerificationCode;
import com.ricky.verification.domain.dto.cmd.CreateFindBackPasswordVerificationCodeCommand;
import com.ricky.verification.domain.dto.cmd.CreateLoginVerificationCodeCommand;
import com.ricky.verification.domain.dto.cmd.CreateRegisterVerificationCodeCommand;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.stream.IntStream;

import static com.ricky.apitest.RandomTestFixture.*;
import static com.ricky.common.constants.ConfigConstants.NO_USER_ID;
import static com.ricky.common.domain.user.UserContext.NOUSER;
import static com.ricky.apitest.verification.VerificationCodeApi.*;
import static com.ricky.verification.domain.VerificationCodeType.LOGIN;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

class VerificationControllerTest extends BaseApiTest {

    @Test
    public void should_create_verification_code_for_register() {
        String mobile = rMobile();

        var command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        String returnId = createVerificationCodeForRegister(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(returnId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_fail_create_verification_code_for_register_if_mobile_is_occupied() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        var command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_fail_create_verification_code_for_register_if_email_is_occupied() {
        String email = rEmail();
        String password = rPassword();
        setupApi.register(email, password);

        var command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(email).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_create_verification_code_for_login() {
        String mobile = rMobile();
        String password = rPassword();
        RegisterResponse response = setupApi.register(mobile, password);

        var command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        String returnId = createVerificationCodeForLogin(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(returnId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());

//        int usedSmsCount = userRepository.byId(response.getUserId()).getResourceUsage().getSmsSentCountForCurrentMonth();
//        assertEquals(1, usedSmsCount);
    }

//    @Test
//    public void should_fail_create_verification_code_for_login_if_max_sms_sent_count_reached() {
//        String mobile = rMobile();
//        String password = rPassword();
//        LoginResponse response = setupApi.registerWithLogin(mobile, password);
//        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
//        assertTrue(verificationCodeRepository.exists(createVerificationCodeForLogin(command)));
//
//        Tenant tenant = tenantRepository.byId(response.getTenantId());
//        IntStream.range(0, tenant.currentPlan().getMaxSmsCountPerMonth() + 1)
//                .forEach(value -> tenant.getResourceUsage().increaseSmsSentCountForCurrentMonth());
//        tenantRepository.save(tenant);
//
//        String newMemberMobile = rMobile();
//        MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), newMemberMobile, rPassword());
//        assertFalse(verificationCodeRepository.exists(createVerificationCodeForLogin(
//                CreateLoginVerificationCodeCommand.builder().mobileOrEmail(newMemberMobile).build())));
//    }

//    @Test
//    public void should_use_extra_remain_sms_count() {
//        String mobile = rMobile();
//        String password = rPassword();
//        LoginResponse response = setupApi.registerWithLogin(mobile, password);
//        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
//        assertTrue(verificationCodeRepository.exists(createVerificationCodeForLogin(command)));
//
//        Tenant tenant = tenantRepository.byId(response.getTenantId());
//        IntStream.range(0, tenant.currentPlan().getMaxSmsCountPerMonth() + 1)
//                .forEach(value -> tenant.getResourceUsage().increaseSmsSentCountForCurrentMonth());
//        tenantRepository.save(tenant);
//        assertFalse(verificationCodeRepository.exists(createVerificationCodeForLogin(command)));
//        int smsCount = tenant.getResourceUsage().getSmsSentCountForCurrentMonth();
//
//        tenant.getPackages().increaseExtraRemainSmsCount(1000);
//        tenantRepository.save(tenant);
//        String newMemberMobile = rMobile();
//        MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), newMemberMobile, rPassword());
//        assertTrue(verificationCodeRepository.exists(createVerificationCodeForLogin(
//                CreateLoginVerificationCodeCommand.builder().mobileOrEmail(newMemberMobile).build())));
//
//        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
//        assertEquals(smsCount + 1, updatedTenant.getResourceUsage().getSmsSentCountForCurrentMonth());
//        assertEquals(999, updatedTenant.getPackages().getExtraRemainSmsCount());
//    }

    @Test
    public void should_fail_create_verification_code_for_login_if_user_not_exists_for_mobile() {
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(rMobile()).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForLogin(command)));
    }

    @Test
    public void should_fail_create_verification_code_for_login_if_user_not_exists_for_email() {
        CreateLoginVerificationCodeCommand command = CreateLoginVerificationCodeCommand.builder().mobileOrEmail(rEmail()).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForLogin(command)));
    }

    @Test
    public void should_create_verification_code_for_find_back_password() {
        String mobile = rMobile();
        String password = rPassword();
        setupApi.register(mobile, password);

        var command = CreateFindBackPasswordVerificationCodeCommand.builder().mobileOrEmail(mobile)
                .build();
        String codeId = createVerificationCodeForFindBackPassword(command);
        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);

        assertNotNull(verificationCode);
        assertEquals(mobile, verificationCode.getMobileOrEmail());
    }

    @Test
    public void should_create_verification_code_for_find_back_password_if_mobile_not_exists_for_user() {
        var command = CreateFindBackPasswordVerificationCodeCommand.builder().mobileOrEmail(rMobile()).build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForFindBackPassword(command)));
    }

    @Test
    public void should_create_verification_code_for_find_back_password_if_email_not_exists_for_user() {
        var command = CreateFindBackPasswordVerificationCodeCommand.builder().mobileOrEmail(rEmail())
                .build();
        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForFindBackPassword(command)));
    }

//    @Test
//    public void should_create_verification_code_for_change_mobile() {
//        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
//
//        String mobile = rMobile();
//        var command = CreateChangeMobileVerificationCodeCommand.builder().mobile(mobile).build();
//        String codeId = createVerificationCodeForChangeMobile(response.getJwt(), command);
//        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);
//
//        assertNotNull(verificationCode);
//        assertEquals(mobile, verificationCode.getMobileOrEmail());
//    }

//    @Test
//    public void should_fail_create_verification_code_for_change_mobile_if_mobile_already_exists_for_user() {
//        String mobile = rMobile();
//        setupApi.registerWithLogin(mobile, rPassword());
//        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
//        var command = CreateChangeMobileVerificationCodeCommand.builder().mobile(mobile).build();
//        assertFalse(verificationCodeRepository.exists(VerificationCodeApi.createVerificationCodeForChangeMobile(response.getJwt(), command)));
//    }
//
//    @Test
//    public void should_create_verification_code_for_identify_mobile() {
//        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
//
//        String mobile = rMobile();
//        IdentifyMobileVerificationCodeCommand command = IdentifyMobileVerificationCodeCommand.builder().mobile(mobile).build();
//        String codeId = createVerificationCodeForIdentifyMobile(response.getJwt(), command);
//        VerificationCode verificationCode = verificationCodeRepository.byId(codeId);
//
//        assertNotNull(verificationCode);
//        assertEquals(mobile, verificationCode.getMobileOrEmail());
//    }

    @Test
    public void should_fail_create_verification_code_resend_within_1_minute() {
        String mobile = rMobile();
        var command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertTrue(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

    @Test
    public void should_fail_create_verification_code_if_too_many_for_today() {
        String mobile = rMobile();
        IntStream.range(1, 22).forEach(value -> {
            VerificationCode verificationCode = new VerificationCode(mobile, LOGIN, NO_USER_ID, NOUSER);
            verificationCodeRepository.save(verificationCode);
        });

        CreateRegisterVerificationCodeCommand command = CreateRegisterVerificationCodeCommand.builder().mobileOrEmail(mobile).build();
        assertFalse(verificationCodeRepository.exists(createVerificationCodeForRegister(command)));
    }

//    @Test
//    public void should_fail_check_verification_code_if_already_used_3_times() {
//        String mobile = rMobile();
//        setupApi.register(mobile, rPassword());
//        VerificationCode code = makeOverUsedVerificationCode(mobile);
//
//        var command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode()).build();
//        assertError(() -> UserApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
//    }

    private VerificationCode makeOverUsedVerificationCode(String mobile) {
        String codeId = createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        code.use();
        code.use();
        code.use();
        verificationCodeRepository.save(code);
        return code;
    }

//    @Test
//    public void should_fail_check_verification_code_if_older_than_10_minutes() {
//        String mobile = rMobile();
//        setupApi.register(mobile, rPassword());
//
//        VerificationCode code = makeExpiredVerificationCode(mobile);
//
//        var command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode()).build();
//        assertError(() -> UserApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
//    }

    private VerificationCode makeExpiredVerificationCode(String mobile) {
        String codeId = createVerificationCodeForLogin(CreateLoginVerificationCodeCommand.builder().mobileOrEmail(mobile).build());
        VerificationCode code = verificationCodeRepository.byId(codeId);
        ReflectionTestUtils.setField(code, "createdAt", Instant.now().minus(11, MINUTES));
        verificationCodeRepository.save(code);
        return code;
    }

//    @Test
//    public void should_fail_check_verification_code_if_wrong_type_provided() {
//        String mobile = rMobile();
//        CreateRegisterVerificationCodeCommand registerVerificationCodeCommand = CreateRegisterVerificationCodeCommand.builder()
//                .mobileOrEmail(mobile).build();
//        String codeId = createVerificationCodeForRegister(registerVerificationCodeCommand);
//        VerificationCode code = verificationCodeRepository.byId(codeId);
//
//        var command = VerificationCodeLoginCommand.builder().mobileOrEmail(mobile).verification(code.getCode()).build();
//        assertError(() -> UserApi.loginWithVerificationCodeRaw(command), VERIFICATION_CODE_CHECK_FAILED);
//    }
}
