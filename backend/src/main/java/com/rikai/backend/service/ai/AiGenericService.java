package com.rikai.backend.service.ai;

import com.rikai.backend.dto.request.agent_ai.GeneratedTestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AiGenericService {
    private final ChatClient chatClient;
    public AiGenericService(
            @Qualifier("creatorClient") ChatClient chatClient
    ) {
        this.chatClient = chatClient;
    }
    public GeneratedTestDto generatedTest(   int questionCount,
                                             String topic,
                                             int evaluationNumber
    ){
        String systemPrompt = """
                You are an expert test generator for backend engineering interviews.

                Your responsibilities:
                - Generate high-quality multiple-choice questions
                - Ensure correctness of answers
                - Ensure questions are practical and not theoretical only
                - Avoid ambiguous questions
                - Always follow strict JSON schema
                - Never include explanations or extra text
                - Output must match the required structure exactly
                """;

        String userPrompt = """
                Generate a multiple-choice test.

                Topic: %s
                Evaluation round: %d
                Number of questions: %d

                Requirements:
                - 4 options per question
                - Exactly 1 correct answer
                - Difficulty: intermediate
                - Focus on real-world backend development scenarios
                """.formatted(topic, evaluationNumber, questionCount);

        return chatClient.prompt()
                .system(systemPrompt)
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .user(userPrompt)
                .call()
                .entity(
                        GeneratedTestDto.class
                );
    }

}
