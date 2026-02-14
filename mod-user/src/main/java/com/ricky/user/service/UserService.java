package com.ricky.user.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.user.command.RegisterCommand;
import com.ricky.user.command.RegisterResponse;
import com.ricky.user.command.UploadAvatarResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    RegisterResponse register(RegisterCommand command);

    UploadAvatarResponse uploadAvatar(MultipartFile avatar, UserContext userContext);
}
