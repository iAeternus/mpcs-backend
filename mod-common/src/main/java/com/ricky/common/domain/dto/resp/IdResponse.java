package com.ricky.common.domain.dto.resp;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdResponse implements Response {

    String id;

    public static IdResponse returnId(String id) {
        return IdResponse.builder().id(id).build();
    }

    @Override
    public String toString() {
        return id;
    }

}
