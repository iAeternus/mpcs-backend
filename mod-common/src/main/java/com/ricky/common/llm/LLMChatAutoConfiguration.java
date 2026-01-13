package com.ricky.common.llm;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.ricky.common.llm.service.LLMChatService;
import com.ricky.common.llm.service.impl.QwenChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static com.alibaba.dashscope.utils.Constants.apiKey;

@AutoConfiguration
public class LLMChatAutoConfiguration {

    @Bean
    @Primary
    ChatClient chatClient(DashScopeChatModel model) {
        return ChatClient.builder(model).build();
    }

}
