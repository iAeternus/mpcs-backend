package com.ricky.common.llm.service.impl;

import com.ricky.common.llm.domain.LLMChatRequest;
import com.ricky.common.llm.domain.LLMChatResponse;
import com.ricky.common.llm.domain.LLMStreamChunk;
import com.ricky.common.llm.service.LLMChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class QwenChatService implements LLMChatService {

    private final ChatClient chatClient;

    @Override
    public LLMChatResponse chat(LLMChatRequest request) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(request.getSystemPrompt())
                .user(request.getUserMessage())
                .call()
                .chatResponse();

        return LLMChatResponse.fromChatResponse(chatResponse);
    }

    @Override
    public Flux<LLMStreamChunk> streamChat(LLMChatRequest request) {
        return chatClient.prompt()
                .system(request.getSystemPrompt())
                .user(request.getUserMessage())
                .stream()
                .chatResponse()
                .map(resp -> {
                    String delta = resp.getResult() != null && resp.getResult().getOutput() != null ? resp.getResult().getOutput().getContent() : "";
                    return LLMStreamChunk.builder()
                            .model(resp.getMetadata().getModel())
                            .delta(delta)
                            .finished(false)
                            .build();
                })
                .concatWith(Flux.just(
                        LLMStreamChunk.builder()
                                .finished(true)
                                .build()
                ));
    }

}
