package com.ricky.user.infra;

import com.ricky.common.exception.MyException;
import com.ricky.common.oss.OssService;
import com.ricky.common.utils.UuidGenerator;
import com.ricky.user.domain.UserAvatarStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.ricky.common.constants.ConfigConstants.AVATAR_BUCKET;
import static com.ricky.common.exception.ErrorCodeEnum.OSS_ERROR;

@Component
@RequiredArgsConstructor
public class OssUserAvatarStorage implements UserAvatarStorage {

    private final OssService ossService;

    @Override
    public String storeAvatar(String userId, MultipartFile avatar) {
        String objectKey = userId + "/" + UuidGenerator.newShortUuid();
        try {
            ossService.ensureBucket(AVATAR_BUCKET);
            ossService.putObject(AVATAR_BUCKET, objectKey, avatar.getInputStream(), avatar.getSize(), avatar.getContentType());
            return objectKey;
        } catch (IOException ex) {
            throw new MyException(OSS_ERROR, "Upload avatar failed", "userId", userId);
        }
    }
}
