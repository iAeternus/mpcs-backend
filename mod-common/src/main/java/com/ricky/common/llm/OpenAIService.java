//package com.ricky.common.llm;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import reactor.core.publisher.Flux;
//
//@Slf4j
//@RequiredArgsConstructor
//@ConditionalOnProperty(name = "spring.ai.provider", havingValue = "openai")
//public class OpenAIService implements LLMService {
//
//    private final ChatClient chatClient;
//
//    @Override
//    public AIResponse chat(AICommand command) {
//        command.correctAndValidate();
//
//        long start = System.currentTimeMillis();
//
//        String content;
//        try {
//            content = chatClient.prompt()
//                    .system(command.getSystemPrompt())
//                    .user(command.getUserPrompt())
//                    .call()
//                    .content();
//        } catch (Exception ex) {
//            log.error("LLM call failed", ex);
//            throw ex;
//        }
//
//        return AIResponse.builder()
//                .content(content)
//                .latencyMs(System.currentTimeMillis() - start)
//                .stream(false)
//                .build();
//    }
//
//    @Override
//    public AIResponse streamChat(AICommand command) {
//        command.correctAndValidate();
//
//        Flux<String> stream = chatClient.prompt()
//                .system(command.getSystemPrompt())
//                .user(command.getUserPrompt())
//                .stream()
//                .content();
//
//        return AIResponse.builder()
//                .stream(true)
//                .streamContent(stream)
//                .build();
//    }
//}
