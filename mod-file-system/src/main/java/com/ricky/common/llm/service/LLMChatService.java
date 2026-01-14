package com.ricky.common.llm.service;

import com.ricky.common.llm.domain.LLMChatRequest;
import com.ricky.common.llm.domain.LLMChatResponse;
import com.ricky.common.llm.domain.LLMStreamChunk;
import reactor.core.publisher.Flux;

/**
 * 大语言模型对话服务
 */
public interface LLMChatService {

    /**
     * 同步对话
     */
    LLMChatResponse chat(LLMChatRequest request);

    /**
     * 流式对话
     */
    Flux<LLMStreamChunk> streamChat(LLMChatRequest request);

}
