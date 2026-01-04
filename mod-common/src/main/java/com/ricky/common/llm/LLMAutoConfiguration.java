package com.ricky.common.llm;

import com.ricky.common.utils.MyObjectMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class LLMAutoConfiguration {

    @Bean
    public LLMService llmService(ChatModel chatModel, MyObjectMapper objectMapper) {
        return new OpenAIService(chatModel, objectMapper);
    }

}
