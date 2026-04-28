package com.rikai.backend.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.memory.ChatMemoryRepository;

//import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
//import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

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
    private Integer maxTokensCreator;

    @Value("${app.ai.router.max-output-tokens}")
    private Integer maxTokensRouter;

    @Value("${app.ai.creator.top-p}")
    private Double topPCreator;

    @Value("${app.ai.router.top-p}")
    private Double topPRouter;

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(30)
                .build();
    }

    @Bean("routerClient")
    public ChatClient routerClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory
    ) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .defaultOptions(ChatOptions.builder()
                        .model(routerModel)
                        .temperature(routerTemperature)
                        .topP(topPRouter)
                        .maxTokens(maxTokensRouter)
                        .build())
                .defaultToolNames("searchPositionTool")
                .build();
    }


    @Bean("creatorClient")
    public ChatClient creatorClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory
    ) {
        return builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .defaultOptions(ChatOptions.builder()
                        .model(creatorModel)
                        .temperature(creatorTemperature)
                        .maxTokens(maxTokensCreator)
                        .topP(topPCreator)
                        .build())
                .build();
    }

}