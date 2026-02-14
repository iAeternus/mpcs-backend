package com.ricky.common.llm;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatProperties;
import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeConnectionProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static com.ricky.common.utils.ValidationUtils.isBlank;

@Configuration
public class LLMChatConfiguration {

    @Bean("dashscopeChatClient")
    public ChatClient dashscopeChatClient(ChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel).build();
    }

    @Bean
    public DashScopeApi dashScopeApi(DashScopeChatProperties chatProperties,
                                     DashScopeConnectionProperties connectionProperties,
                                     ObjectProvider<ResponseErrorHandler> responseErrorHandlerProvider,
                                     DashScopeObjectMapperProvider dashScopeObjectMapperProvider) {
        String apiKey = firstNonBlank(chatProperties.getApiKey(), connectionProperties.getApiKey());
        String baseUrl = firstNonBlank(chatProperties.getBaseUrl(), connectionProperties.getBaseUrl());
        String workspaceId = firstNonBlank(chatProperties.getWorkspaceId(), connectionProperties.getWorkspaceId());

        DashScopeApi.Builder builder = DashScopeApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .workSpaceId(workspaceId)
                .headers(new LinkedMultiValueMap<>());

        RestClient.Builder restClientBuilder = RestClient.builder()
                .messageConverters(converters -> {
                    converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                    converters.add(new MappingJackson2HttpMessageConverter(dashScopeObjectMapperProvider.get()));
                });

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(dashScopeObjectMapperProvider.get()));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(dashScopeObjectMapperProvider.get()));
                })
                .build();

        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeStrategies(exchangeStrategies);

        ResponseErrorHandler responseErrorHandler = responseErrorHandlerProvider.getIfAvailable(DefaultResponseErrorHandler::new);

        builder.restClientBuilder(restClientBuilder);
        builder.webClientBuilder(webClientBuilder);
        builder.responseErrorHandler(responseErrorHandler);

        return builder.build();
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        return second;
    }

    @Bean
    public DashScopeObjectMapperProvider dashScopeObjectMapperProvider() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.deactivateDefaultTyping();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(DashScopeApiSpec.ChatCompletionMessage.class, DashScopeChatCompletionMessageMixin.class);
        return new DashScopeObjectMapperProvider(mapper);
    }

    private abstract static class DashScopeChatCompletionMessageMixin {
        @JsonProperty("content")
        abstract Object rawContent();
    }

    public static final class DashScopeObjectMapperProvider {
        private final ObjectMapper objectMapper;

        public DashScopeObjectMapperProvider(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public ObjectMapper get() {
            return objectMapper;
        }
    }
}
