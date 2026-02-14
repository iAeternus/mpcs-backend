package com.ricky.user.domain;

import org.springframework.web.multipart.MultipartFile;

public interface UserAvatarStorage {

    String storeAvatar(String userId, MultipartFile avatar);

}
