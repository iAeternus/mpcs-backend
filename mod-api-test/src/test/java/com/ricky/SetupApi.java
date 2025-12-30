package com.ricky;

import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.file.domain.FileExtension;
import com.ricky.file.domain.MimeType;
import com.ricky.login.LoginApi;
import com.ricky.user.UserApi;
import com.ricky.user.domain.dto.cmd.RegisterCommand;
import com.ricky.user.domain.dto.resp.RegisterResponse;
import com.ricky.verification.VerificationCodeApi;
import com.ricky.verification.domain.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.ricky.RandomTestFixture.*;

@Component
@RequiredArgsConstructor
public class SetupApi {

    private final VerificationCodeRepository verificationCodeRepository;

    public RegisterResponse register(String mobileOrEmail, String password) {
        return register(rUsername(), mobileOrEmail, password);
    }

    public RegisterResponse register(String username, String mobileOrEmail, String password) {
        String verificationCodeId = VerificationCodeApi.createVerificationCodeForRegister(mobileOrEmail);
        String code = verificationCodeRepository.byId(verificationCodeId).getCode();

        RegisterCommand command = RegisterCommand.builder()
                .mobileOrEmail(mobileOrEmail)
                .verification(code)
                .password(password)
                .username(username)
                .agreement(true)
                .build();

        return UserApi.register(command);
    }

    public LoginResponse registerWithLogin(String mobileOrEmail, String password) {
        RegisterResponse response = register(mobileOrEmail, password);
        String jwt = LoginApi.loginWithMobileOrEmail(mobileOrEmail, password);
        return LoginResponse.builder()
                .userId(response.getUserId())
                .jwt(jwt)
                .build();
    }

    public LoginResponse registerWithLogin() {
        return registerWithLogin(rMobile(), rPassword());
    }

    /**
     * @param path 相对于测试资源目录下的文件路径，在其中要指定文件名和扩展名，例如 data/plain-text-file.txt
     * @param name 表单名称
     * @return 文件
     * @brief 加载测试文件
     */
    public static MultipartFile loadTestFile(String path, String name) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String filename = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;
            FileExtension fileExtension = FileExtension.fromFilename(filename);
            MimeType mimeType = MimeType.fromExtension(fileExtension);

            return new MockMultipartFile(
                    name,
                    filename,
                    mimeType.getContentType(),
                    StreamUtils.copyToByteArray(resource.getInputStream())
            );
        } catch (IOException e) {
            throw new RuntimeException("无法读取测试文件: " + path, e);
        }
    }
}
