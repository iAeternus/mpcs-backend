package com.ricky.common.llm;

public interface LLMService {

    AIResponse chat(AICommand command);

    AIResponse streamChat(AICommand command);

}
