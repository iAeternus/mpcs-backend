package com.ricky.user.domain.dto.resp;

import com.ricky.common.domain.marker.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class RegisterResponse implements Response {

    String userId;

}
