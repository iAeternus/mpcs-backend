package com.ricky.user.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.validation.id.Id;
import com.ricky.folder.query.FolderHierarchyResponse;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import jakarta.validation.constraints.NotBlank;

public interface UserQueryService {
    UserProfileResponse fetchMyProfile(UserContext userContext);

    UserInfoResponse fetchMyUserInfo(UserContext userContext);

    UserInfoResponse fetchUserInfo(String userId);
}
