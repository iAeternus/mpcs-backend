//package com.ricky.common.llm;
//
//import com.ricky.common.llm.service.LLMChatService;
//import com.ricky.common.llm.service.impl.QwenChatService;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.context.annotation.Bean;
//
//@AutoConfiguration
//public class LLMChatAutoConfiguration {
//
//    @Bean("dashscopeChatClient")
//    public ChatClient dashscopeChatClient(ChatModel dashscopeChatModel) {
//        return ChatClient.builder(dashscopeChatModel).build();
//    }
//
//    @Bean
//    public LLMChatService llmChatService(ChatClient dashscopeChatModel) {
//        return new QwenChatService(dashscopeChatModel);
//    }
//
//}
