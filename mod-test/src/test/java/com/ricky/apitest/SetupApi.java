package com.ricky.apitest;

import com.ricky.apitest.folder.FolderApi;
import com.ricky.apitest.login.LoginApi;
import com.ricky.apitest.upload.FileUploadApi;
import com.ricky.apitest.user.UserApi;
import com.ricky.apitest.verification.VerificationCodeApi;
import com.ricky.common.domain.dto.resp.LoginResponse;
import com.ricky.common.hash.FileHasherFactory;
import com.ricky.file.domain.File;
import com.ricky.file.domain.FileRepository;
import com.ricky.folder.command.CreateFolderCommand;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.upload.domain.StorageService;
import com.ricky.upload.domain.event.FileUploadedLocalEvent;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.domain.event.UserCreatedEvent;
import com.ricky.verification.domain.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.ricky.apitest.RandomTestFixture.*;
import static com.ricky.common.domain.SpaceType.personalCustomId;
import static com.ricky.common.event.DomainEventType.USER_CREATED;

@Component
@RequiredArgsConstructor
public class SetupApi {

    private final VerificationCodeRepository verificationCodeRepository;
    private final FileHasherFactory fileHasherFactory;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final EventUtils eventUtils;

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

        RegisterResponse resp = UserApi.register(command);
        eventUtils.awaitLatestEventConsumed(resp.getUserId(), USER_CREATED, UserCreatedEvent.class);
        return resp;
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

    public TestFileContext registerWithFile(String path) throws IOException {
        LoginResponse manager = registerWithLogin();
        String customId = personalCustomId(manager.getUserId());

        ClassPathResource resource = new ClassPathResource(path);
        java.io.File file = resource.getFile();

        Folder root = folderRepository.getRoot(customId);

        String fileHash = deleteFileWithSameHash(file);
        String fileId = FileUploadApi.upload(manager.getJwt(), file, root.getId()).getFileId();

        eventUtils.awaitLatestLocalEventConsumed(fileId, FileUploadedLocalEvent.class);

        return TestFileContext.builder()
                .manager(manager)
                .customId(customId)
                .parentId(root.getId())
                .fileHash(fileHash)
                .fileId(fileId)
                .originalFile(file)
                .build();
    }

    public String deleteFileWithSameHash(java.io.File file) throws IOException {
        String fileHash;
        try (InputStream in = Files.newInputStream(file.toPath())) {
            fileHash = fileHasherFactory.getFileHasher().hash(in);
        }

        List<File> files = fileRepository.listByFileHash(fileHash);
        storageService.delete(files.stream().map(File::getStorageId).collect(toImmutableList()));
        fileRepository.delete(files);

        return fileHash;
    }

    public String createFolderUnderRoot(String token, String customId, String folderName) {
        Folder root = folderRepository.getRoot(customId);
        CreateFolderCommand command = CreateFolderCommand.builder()
                .customId(customId)
                .parentId(root.getId())
                .folderName(folderName)
                .build();
        return FolderApi.createFolder(token, command);
    }
}
