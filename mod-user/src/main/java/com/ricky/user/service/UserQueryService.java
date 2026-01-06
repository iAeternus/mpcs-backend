package com.ricky.user.service;

import com.ricky.common.domain.user.UserContext;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;

public interface UserQueryService {
    UserProfileResponse fetchMyProfile(UserContext userContext);

    UserInfoResponse fetchMyUserInfo(UserContext userContext);
}
