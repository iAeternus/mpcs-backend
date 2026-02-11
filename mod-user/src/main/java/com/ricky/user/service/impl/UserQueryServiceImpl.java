package com.ricky.user.service.impl;

import com.ricky.common.domain.user.UserContext;
import com.ricky.common.ratelimit.RateLimiter;
import com.ricky.user.domain.User;
import com.ricky.user.domain.UserRepository;
import com.ricky.user.query.UserInfoResponse;
import com.ricky.user.query.UserProfileResponse;
import com.ricky.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final RateLimiter rateLimiter;
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse fetchMyProfile(UserContext userContext) {
        rateLimiter.applyFor(userContext.getUid(), "User:FetchMyProfile", 100);

        User user = userRepository.cachedById(userContext.getUid());
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .mobileIdentified(user.isMobileIdentified())
                .build();
    }

    @Override
    public UserInfoResponse fetchMyUserInfo(UserContext userContext) {
        rateLimiter.applyFor(userContext.getUid(), "User:FetchMyUserInfo", 10);

        User user = userRepository.cachedById(userContext.getUid());
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .customId(user.getCustomId())
                .build();
    }
}
