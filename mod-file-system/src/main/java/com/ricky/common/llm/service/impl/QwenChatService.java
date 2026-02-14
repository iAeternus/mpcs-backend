package com.ricky.common.llm.service.impl;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ricky.common.llm.LLMChatConfiguration.DashScopeObjectMapperProvider;
import com.ricky.common.llm.domain.LLMChatRequest;
import com.ricky.common.llm.domain.LLMChatResponse;
import com.ricky.common.llm.domain.LLMStreamChunk;
import com.ricky.common.llm.service.LLMChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ricky.common.utils.ValidationUtils.isNotBlank;

@Component
@Slf4j
@RequiredArgsConstructor
public class QwenChatService implements LLMChatService {

    private final DashScopeApi dashScopeApi;
    private final DashScopeChatProperties chatProperties;
    private final DashScopeObjectMapperProvider objectMapperProvider;

    @Override
    public LLMChatResponse chat(LLMChatRequest request) {
        String model = resolveModel();
        DashScopeApiSpec.ChatCompletionRequest completionRequest = buildChatRequest(request, model, false);
        logRequestJson(completionRequest);
        ResponseEntity<DashScopeApiSpec.ChatCompletion> responseEntity = dashScopeApi.chatCompletionEntity(completionRequest);
        DashScopeApiSpec.ChatCompletion completion = responseEntity.getBody();

        String text = extractText(completion);
        DashScopeApiSpec.TokenUsage usage = completion != null ? completion.usage() : null;

        return LLMChatResponse.builder()
                .model(model)
                .text(text)
                .usage(toUsage(usage))
                .build();
    }

    // TODO 这个方法还存在问题
    @Override
    public Flux<LLMStreamChunk> streamChat(LLMChatRequest request) {
        String model = resolveModel();
        DashScopeApiSpec.ChatCompletionRequest completionRequest = buildChatRequest(request, model, true);
        logRequestJson(completionRequest);

        AtomicBoolean logged = new AtomicBoolean(false);
        return dashScopeApi.chatCompletionStream(completionRequest, new LinkedMultiValueMap<>())
                .doOnNext(chunk -> {
                    if (logged.compareAndSet(false, true)) {
                        log.info("DashScope stream first chunk: {}", chunk);
                    }
                })
                .map(chunk -> {
                    String delta = extractText(chunk);
                    return LLMStreamChunk.builder()
                            .model(model)
                            .delta(delta)
                            .finished(false)
                            .build();
                })
                .filter(chunk -> isNotBlank(chunk.getDelta()))
                .concatWith(Flux.just(
                        LLMStreamChunk.builder()
                                .model(model)
                                .finished(true)
                                .build()
                ));
    }

    private DashScopeApiSpec.ChatCompletionRequest buildChatRequest(LLMChatRequest request, String model, boolean stream) {
        List<DashScopeApiSpec.ChatCompletionMessage> messages = new ArrayList<>(2);
        if (isNotBlank(request.getSystemPrompt())) {
            messages.add(buildMessage(request.getSystemPrompt(), DashScopeApiSpec.ChatCompletionMessage.Role.SYSTEM));
        }
        messages.add(buildMessage(request.getUserMessage(), DashScopeApiSpec.ChatCompletionMessage.Role.USER));

        DashScopeApiSpec.ChatCompletionRequestInput input = new DashScopeApiSpec.ChatCompletionRequestInput(messages);
        DashScopeApiSpec.ChatCompletionRequestParameter parameters = new DashScopeApiSpec.ChatCompletionRequestParameter();

        return new DashScopeApiSpec.ChatCompletionRequest(model, input, parameters, stream, Boolean.FALSE);
    }

    private DashScopeApiSpec.ChatCompletionMessage buildMessage(String text, DashScopeApiSpec.ChatCompletionMessage.Role role) {
        return new DashScopeApiSpec.ChatCompletionMessage(text, role);
    }

    private String resolveModel() {
        if (chatProperties.getOptions() != null && isNotBlank(chatProperties.getOptions().getModel())) {
            return chatProperties.getOptions().getModel();
        }
        return DashScopeApi.DEFAULT_CHAT_MODEL;
    }

    private String extractText(DashScopeApiSpec.ChatCompletion completion) {
        if (completion == null || completion.output() == null) {
            return "";
        }
        return extractTextFromOutput(completion.output());
    }

    private String extractText(DashScopeApiSpec.ChatCompletionChunk chunk) {
        if (chunk == null || chunk.output() == null) {
            return "";
        }
        return extractTextFromOutput(chunk.output());
    }

    private String extractTextFromOutput(DashScopeApiSpec.ChatCompletionOutput output) {
        if (output == null) {
            return "";
        }

        if (isNotBlank(output.text())) {
            return output.text();
        }

        if (output.choices() == null || output.choices().isEmpty()) {
            return "";
        }

        DashScopeApiSpec.ChatCompletionMessage message = output.choices().get(0).message();
        if (message == null || message.rawContent() == null) {
            return "";
        }

        Object raw = message.rawContent();
        if (raw instanceof String text) {
            return text;
        }

        if (raw instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof DashScopeApiSpec.ChatCompletionMessage.MediaContent) {
            @SuppressWarnings("unchecked")
            List<DashScopeApiSpec.ChatCompletionMessage.MediaContent> content =
                    (List<DashScopeApiSpec.ChatCompletionMessage.MediaContent>) raw;
            return DashScopeApi.getTextContent(content);
        }

        return "";
    }

    private LLMChatResponse.Usage toUsage(DashScopeApiSpec.TokenUsage usage) {
        if (usage == null) {
            return null;
        }
        return LLMChatResponse.Usage.builder()
                .promptTokens(usage.inputTokens())
                .completionTokens(usage.outputTokens())
                .totalTokens(usage.totalTokens())
                .build();
    }

    private void logRequestJson(DashScopeApiSpec.ChatCompletionRequest request) {
        try {
            log.info("DashScope request json: {}", objectMapperProvider.get().writeValueAsString(request));
        } catch (JsonProcessingException ignored) {
        }
    }
}
