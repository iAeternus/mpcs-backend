package com.ricky.common.llm;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO 使用springAI和ricky-bot的成熟方案
@Slf4j
@RequiredArgsConstructor
public class DeepSeekService implements LLMService {

    private final WebClient.Builder webClientBuilder;
    private final DeepSeekChatProperties chatProperties;

    @Override
    public AIResponse chat(AICommand command) {
        command.correctAndValidate();

        WebClient client = webClientBuilder
                .baseUrl(chatProperties.getBaseUrl()) // https://api.deepseek.com/v1
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + chatProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("model", chatProperties.getModel());
        body.put("temperature", chatProperties.getTemperature());
        body.put("max_tokens", chatProperties.getMaxTokens());

        List<Map<String, Object>> messages = new ArrayList<>();

        if (command.getSystemPrompt() != null) {
            messages.add(Map.of(
                    "role", "system",
                    "content", command.getSystemPrompt()
            ));
        }

        messages.add(Map.of(
                "role", "user",
                "content", command.getUserPrompt()
        ));

        body.put("messages", messages);

        long start = System.currentTimeMillis();

        String content = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.at("/choices/0/message/content").asText())
                .doOnError(e ->
                        log.error("DeepSeek LLM call failed, requestBody={}", body, e)
                )
                .block();

        return AIResponse.builder()
                .content(content)
                .latencyMs(System.currentTimeMillis() - start)
                .stream(false)
                .build();
    }

    @Override
    public AIResponse streamChat(AICommand command) {
        throw new UnsupportedOperationException("DeepSeek streamChat not implemented yet");
    }
}
