package com.ricky.common.llm.domain;

import com.ricky.common.domain.marker.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.ai.chat.model.ChatResponse;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LLMChatResponse implements Response {

    String model;
    String text;
    Usage usage;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Usage {
        Integer promptTokens;
        Integer completionTokens;
        Integer totalTokens;
    }

    public static LLMChatResponse fromChatResponse(ChatResponse chatResponse) {
        return LLMChatResponse.builder()
                .model(chatResponse.getMetadata().getModel())
                .text(chatResponse.getResult().getOutput().getText())
                .usage(Usage.builder()
                        .promptTokens(chatResponse.getMetadata().getUsage().getPromptTokens())
                        .completionTokens(chatResponse.getMetadata().getUsage().getCompletionTokens())
                        .totalTokens(chatResponse.getMetadata().getUsage().getTotalTokens())
                        .build())
                .build();
    }

}
