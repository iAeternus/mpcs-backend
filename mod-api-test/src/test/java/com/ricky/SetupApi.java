package com.ricky;

import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.login.LoginApi;
import com.ricky.upload.domain.FileStorage;
import com.ricky.user.UserApi;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.verification.VerificationCodeApi;
import com.ricky.verification.domain.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.RandomTestFixture.*;

@Component
@RequiredArgsConstructor
public class SetupApi {

    private final VerificationCodeRepository verificationCodeRepository;
    private final FileHasherFactory fileHasherFactory;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

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

    public String deleteFileWithSameHash(java.io.File file) throws IOException {
        String fileHash;
        try (InputStream in = Files.newInputStream(file.toPath())) {
            fileHash = fileHasherFactory.getFileHasher().hash(in);
        }

        List<File> files = fileRepository.listByFileHash(fileHash);
        fileStorage.delete(files.stream().map(File::getStorageId).collect(toImmutableList()));
        fileRepository.delete(files);

        return fileHash;
    }
}
