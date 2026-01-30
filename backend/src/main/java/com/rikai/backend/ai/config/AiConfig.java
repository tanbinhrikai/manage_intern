package com.rikai.backend.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${app.ai.router.model}")
    private String routerModel;

    @Value("${app.ai.router.temperature}")
    private Double routerTemperature;

    @Value("${app.ai.creator.model}")
    private String creatorModel;

    @Value("${app.ai.creator.temperature}")
    private Double creatorTemperature;

    @Value("${app.ai.creator.max-output-tokens}")
    private Integer maxTokens;

    @Bean("routerClient")
    public ChatClient routerClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder()
                        .model(routerModel)
                        .temperature(routerTemperature)
                        .maxTokens(maxTokens)
                        .build())
                .build();
    }

    @Bean("creatorClient")
    public ChatClient creatorClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder()
                        .model(creatorModel)
                        .temperature(creatorTemperature)
                        .maxTokens(maxTokens)
                        .build())
                .build();
    }
}