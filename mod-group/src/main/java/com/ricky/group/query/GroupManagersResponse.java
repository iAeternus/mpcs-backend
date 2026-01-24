package com.ricky.group.query;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupManagersResponse implements Response {

    List<Manager> groupManagers;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Manager {
        String username;
        String mobileOrEmail;
    }

}
