package com.ricky.common.llm;

import com.ricky.common.utils.MyObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ricky.common.utils.ValidationUtils.isNotBlank;
import static com.ricky.common.utils.ValidationUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ai.provider", havingValue = "openai")
public class OpenAIService implements LLMService {

    private final ChatModel chatModel;
    private final MyObjectMapper objectMapper;

    @Override
    @Retryable(
            retryFor = {ResourceAccessException.class, IllegalStateException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public AIResponse chat(AICommand command) {
        Prompt prompt = buildPromptFromCommand(command);

        long startTime = System.currentTimeMillis();
        ChatResponse chatResponse = chatModel.call(prompt);
        long latency = System.currentTimeMillis() - startTime;

        return buildAIResponse(chatResponse, command, latency);
    }

    @Override
    public AIResponse streamChat(AICommand command) {
        Prompt prompt = buildPromptFromCommand(command);

        Flux<String> contentStream = chatModel.stream(prompt)
                .map(response -> {
                    String content = response.getResult().getOutput().getContent();
                    return (content != null) ? content : "";
                })
                .onErrorReturn("【流式响应中断】")
                .doOnComplete(() -> log.debug("Stream completed for command: {}", command.getUserPrompt()));

        return AIResponse.builder()
                .content("【此为流式响应，请消费streamContent字段】")
                .model(determineModelName(command))
                .stream(true)
                .streamContent(contentStream)
                .build();
    }

    /**
     * 从AICommand构建Spring AI Prompt
     */
    private Prompt buildPromptFromCommand(AICommand command) {
        List<Message> messages = new ArrayList<>();

        if (isNotBlank(command.getSystemPrompt())) {
            messages.add(new SystemMessage(command.getSystemPrompt()));
        }

        if (isNotEmpty(command.getContextList())) {
            String context = String.join("\n", command.getContextList());
            messages.add(new SystemMessage("上下文信息：\n" + context));
        }

        messages.add(new UserMessage(command.getUserPrompt()));

        Prompt prompt = new Prompt(messages);

        if (command.getOptions() != null && !command.getOptions().isEmpty()) {
            ChatOptions options = convertOptions(command.getOptions());
            prompt = new Prompt(messages, options);
        }

        return prompt;
    }

    /**
     * 将Map格式的options转换为Spring AI的ChatOptions
     */
    private ChatOptions convertOptions(Map<String, Object> optionsMap) {
        String json = objectMapper.writeValueAsString(optionsMap);
        return objectMapper.readValue(json, OpenAiChatOptions.class);
    }

    /**
     * 从Spring AI响应构建统一的AIResponse
     */
    private AIResponse buildAIResponse(ChatResponse chatResponse, AICommand command, long latency) {
        String content = chatResponse.getResult().getOutput().getContent();
        content = isNotBlank(content) ? content : "【模型返回空响应】";
        return AIResponse.builder()
                .content(content)
                .model(determineModelName(command))
                .latencyMs(latency)
                .stream(false)
                .build();
    }

//    /**
//     * 提取Token使用量（兼容不同响应结构）
//     */
//    private Long extractTokenUsage(ChatResponse response) {
//        try {
//            // 方式1：从Usage元数据提取
//            if (response.getMetadata() != null) {
//                Object usage = response.getMetadata().get("usage");
//                if (usage instanceof Map) {
//                    Object totalTokens = ((Map<?, ?>) usage).get("totalTokens");
//                    if (totalTokens instanceof Number) {
//                        return ((Number) totalTokens).longValue();
//                    }
//                }
//            }
//
//            // 方式2：从Result中提取
//            return response.getResult().getMetadata().getTokenUsage().getTotalTokens();
//        } catch (Exception e) {
//            log.debug("Unable to extract token usage", e);
//            return 0L; // 无法提取时返回0
//        }
//    }

    /**
     * 确定实际使用的模型名称
     */
    private String determineModelName(AICommand command) {
        // 优先级：options中指定的模型 > 配置的默认模型 > 未知
        if (command.getOptions() != null && command.getOptions().containsKey("model")) {
            return command.getOptions().get("model").toString();
        }
        // 可以通过environment或配置类获取实际配置的模型名
        return "openai-model";
    }
}