package com.ricky.user.service.impl;

import com.ricky.common.domain.user.Role;
import com.ricky.common.domain.user.UserContext;
import com.ricky.common.exception.MyException;
import com.ricky.common.password.IPasswordEncoder;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.folder.domain.Folder;
import com.ricky.folder.domain.FolderRepository;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.command.UploadAvatarResponse;
import com.ricky.user.domain.CreateUserResult;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserAvatarStorage;
import com.ricky.user.domain.UserDomainService;
import com.ricky.user.domain.UserRepository;
import com.ricky.user.infra.AvatarUrlResolver;
import com.ricky.user.service.UserService;
import com.ricky.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static com.ricky.common.constants.ConfigConstants.AVATAR_TYPES;
import static com.ricky.common.exception.ErrorCodeEnum.FILE_MUST_NOT_BE_EMPTY;
import static com.ricky.common.exception.ErrorCodeEnum.UNSUPPORTED_FILE_TYPES;
import static com.ricky.common.utils.ValidationUtils.isBlank;
import static com.ricky.verification.domain.VerificationCodeType.REGISTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RateLimiter rateLimiter;
    private final VerificationCodeChecker verificationCodeChecker;
    private final IPasswordEncoder passwordEncoder;
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final UserAvatarStorage userAvatarStorage;
    private final AvatarUrlResolver avatarUrlResolver;

    @Override
    @Transactional
    public RegisterResponse register(RegisterCommand command) {
        rateLimiter.applyFor("Registration:Register:All", 20);

        String mobileOrEmail = command.getMobileOrEmail();
        verificationCodeChecker.check(mobileOrEmail, command.getVerification(), REGISTER);

        UserContext userContext = UserContext.of(User.newUserId(), command.getUsername(), Role.NORMAL_USER);
        String encodedPassword = passwordEncoder.encode(command.getPassword());
        CreateUserResult result = userDomainService.register(
                mobileOrEmail,
                encodedPassword,
                userContext);

        User user = result.getUser();
        Folder root = result.getRoot();

        userRepository.save(user);
        folderRepository.save(root);
        log.info("Registered user[{}]", user.getId());

        return RegisterResponse.builder().userId(user.getId()).build();
    }

    @Override
    @Transactional
    public UploadAvatarResponse uploadAvatar(MultipartFile avatar, UserContext userContext) {
        rateLimiter.applyFor(userContext.getUid(), "User:UploadAvatar", 20);

        if (avatar.isEmpty()) {
            throw new MyException(FILE_MUST_NOT_BE_EMPTY, "Avatar must not be empty");
        }

        String contentType = avatar.getContentType();
        if (isBlank(contentType) || !Arrays.asList(AVATAR_TYPES).contains(contentType)) {
            throw new MyException(UNSUPPORTED_FILE_TYPES, "Unsupported avatar content type", "contentType", contentType);
        }

        User user = userRepository.byId(userContext.getUid());
        String objectKey = userAvatarStorage.storeAvatar(user.getId(), avatar);

        user.updateAvatarUrl(avatarUrlResolver.toPublicUrl(objectKey), userContext);
        userRepository.save(user);
        log.info("User[{}] avatar uploaded", user.getId());

        return UploadAvatarResponse.builder()
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
