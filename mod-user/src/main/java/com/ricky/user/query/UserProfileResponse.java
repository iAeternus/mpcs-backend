package com.ricky.user.query;

import com.ricky.common.domain.marker.Response;
import com.ricky.common.domain.user.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileResponse implements Response {

    String userId;
    String username;
    String mobileOrEmail;
    Role role;
    String avatarUrl; // TODO 考虑换成 UploadedFile
    boolean mobileIdentified;

}
