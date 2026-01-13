package com.ricky.common.llm.domain;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LLMStreamChunk implements Response {

    String model;
    String delta;
    boolean finished;

}
