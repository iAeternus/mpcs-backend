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
    String content;
    Usage usage;

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Usage {
        Long promptTokens;
        Long generationTokens;
        Long totalTokens;
    }

    public static LLMChatResponse fromChatResponse(ChatResponse chatResponse) {
        return LLMChatResponse.builder()
                .model(chatResponse.getMetadata().getModel())
                .content(chatResponse.getResult().getOutput().getContent())
                .usage(Usage.builder()
                        .promptTokens(chatResponse.getMetadata().getUsage().getPromptTokens())
                        .generationTokens(chatResponse.getMetadata().getUsage().getGenerationTokens())
                        .totalTokens(chatResponse.getMetadata().getUsage().getTotalTokens())
                        .build())
                .build();
    }

}
