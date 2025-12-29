package com.ricky.common.exception;

import com.ricky.common.domain.marker.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ErrorResponse implements Response {

    MyError error;

}
