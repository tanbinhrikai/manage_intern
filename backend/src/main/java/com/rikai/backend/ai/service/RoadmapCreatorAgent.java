package com.rikai.backend.ai.service;

import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.ai.prompt.PromptManager;
import com.rikai.backend.repository.InternshipBatchRepository;
import com.rikai.backend.repository.PositionRepository;
import com.rikai.backend.repository.RoadmapNodeRepository;
import com.rikai.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component

public class RoadmapCreatorAgent {

    private final ChatClient routerClient;
    private final ChatClient creatorClient;

    public RoadmapCreatorAgent(
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("creatorClient") ChatClient creatorClient
    ) {
        this.routerClient = routerClient;
        this.creatorClient = creatorClient;
    }

    public RoadmapGenerationDto generate(String topic, String notes) {
        String finalPrompt = PromptManager.ROADMAP_GENERATION_SYSTEM.formatted(topic, null)
                + "\nGhi chú thêm: " + notes;
        return creatorClient.prompt()
                .user(finalPrompt)
                .call()
                .entity(RoadmapGenerationDto.class);
    }
}