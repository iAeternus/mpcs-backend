package com.ricky.common.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
public class DeepSeekChatProperties {

    private String apiKey;
    private String baseUrl = "https://api.deepseek.com/v1";
    private String model = "deepseek-chat";

    private double temperature = 0.7;
    private int maxTokens = 300;

}
