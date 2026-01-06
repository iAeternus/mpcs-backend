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
public class UserInfoResponse implements Response {

    String userId;
    String username;
    String email;
    String mobile;
    Role role;

}
