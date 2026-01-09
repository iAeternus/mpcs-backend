package com.ricky.common.llm;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
public class LLMAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.ai.deepseek.chat")
    public DeepSeekChatProperties deepSeekChatProperties() {
        return new DeepSeekChatProperties();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ai.provider", havingValue = "deepseek")
    public LLMService deepSeekService(
            WebClient.Builder webClientBuilder,
            DeepSeekChatProperties chatProperties
    ) {
        return new DeepSeekService(webClientBuilder, chatProperties);
    }

//    @Bean
//    public ChatClient chatClient(ChatModel chatModel) {
//        return ChatClient.builder(chatModel).build();
//    }

//    @Bean
//    public LLMService llmService(ChatClient chatClient) {
//        return new OpenAIService(chatClient);
//    }

}
