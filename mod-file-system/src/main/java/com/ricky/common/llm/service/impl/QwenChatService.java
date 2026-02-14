package com.ricky.common.llm.service.impl;

import com.ricky.common.llm.domain.LLMChatRequest;
import com.ricky.common.llm.domain.LLMChatResponse;
import com.ricky.common.llm.domain.LLMStreamChunk;
import com.ricky.common.llm.service.LLMChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

import static com.ricky.common.utils.ValidationUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class QwenChatService implements LLMChatService {

    private final ChatClient chatClient;

    @Override
    public LLMChatResponse chat(LLMChatRequest request) {
        Prompt prompt = buildPrompt(request);
        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();

        return LLMChatResponse.fromChatResponse(chatResponse);
    }

    @Override
    public Flux<LLMStreamChunk> streamChat(LLMChatRequest request) {
        Prompt prompt = buildPrompt(request);
        return chatClient.prompt(prompt)
                .stream()
                .content()
                .map(delta -> LLMStreamChunk.builder()
                        .delta(delta)
                        .finished(false)
                        .build())
                .concatWith(Flux.just(
                        LLMStreamChunk.builder()
                                .finished(true)
                                .build()
                ));
    }

    private Prompt buildPrompt(LLMChatRequest request) {
        List<Message> messages = new ArrayList<>(2);
        if (isNotBlank(request.getSystemPrompt())) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }
        messages.add(new UserMessage(request.getUserMessage()));
        return new Prompt(messages);
    }
}
