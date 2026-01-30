package com.rikai.backend.ai.agent;

import com.rikai.backend.ai.dto.response.UserIntentDto;
import com.rikai.backend.ai.prompt.PromptManager;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IntentRouterAgent {
    private final ChatClient chatClient;

    public IntentRouterAgent(@Qualifier("routerClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public UserIntentDto analyze(String userMessage) {
        return chatClient.prompt()
                .system(PromptManager.INTENT_ANALYSIS_SYSTEM)
                .user(userMessage)
                .call()
                .entity(UserIntentDto.class);
    }
}