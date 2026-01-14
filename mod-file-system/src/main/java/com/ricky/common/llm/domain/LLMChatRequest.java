package com.ricky.common.llm.domain;

import com.ricky.common.domain.marker.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LLMChatRequest implements Command {

    @NotBlank
    String userMessage;
    String systemPrompt;

}
