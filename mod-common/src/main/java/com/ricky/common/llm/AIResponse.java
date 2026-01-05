package com.ricky.common.llm;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import reactor.core.publisher.Flux;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AIResponse implements Response {

    String content;
    Long latencyMs; // 请求耗时，单位：毫秒
    Boolean stream;
    Flux<String> streamContent; // 流式响应

}