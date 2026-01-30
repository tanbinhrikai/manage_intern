package com.rikai.backend.ai.service;

import com.rikai.backend.ai.agent.IntentRouterAgent;
import com.rikai.backend.ai.dto.request.PositionSelectionDto;
import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.ai.dto.response.UserIntentDto;
import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.model.Position;
import com.rikai.backend.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.rikai.backend.ai.dto.response.ChatResponseDto.ActionType.DISPLAY_ROADMAP;


@Service
@RequiredArgsConstructor
public class AIAssistantService {

    private final IntentRouterAgent routerAgent;
    private final RoadmapCreatorAgent creatorAgent;
    private final PositionRepository positionRepository;

    @Transactional
    public ChatResponseDto processMessage(String message, Long positionId, String duration) {
        UserIntentDto intent = routerAgent.analyze(message);

        if (!intent.isRoadmapRequest()) {
            return ChatResponseDto.builder()
                    .action(ChatResponseDto.ActionType.NORMAL_CHAT)
                    .message(intent.conversationalReply())
                    .build();
        }

        Position position = null;
        if (positionId != null) {
            position = positionRepository.findById(positionId).orElse(null);
        }

        if (position == null && intent.detectedTopic() != null) {
            position = positionRepository.findByTitleContainingIgnoreCase(intent.detectedTopic())
                    .stream().findFirst().orElse(null);
        }

        if (position == null) {
            List<PositionSelectionDto> allPositions = positionRepository.findAll()
                    .stream()
                    .map(p -> new PositionSelectionDto(p.getId(), p.getTitle()))
                    .toList();

            return ChatResponseDto.builder()
                    .action(ChatResponseDto.ActionType.SELECT_POSITION)
                    .message("Tôi chưa rõ bạn muốn tạo lộ trình cho vị trí nào. Vui lòng chọn bên dưới:")
                    .data(allPositions)
                    .build();
        }

        String finalDuration = (duration != null && !duration.isEmpty())
                ? duration
                : intent.detectedDuration();

        if (finalDuration == null || finalDuration.isEmpty()) {
            List<String> suggestedDurations = List.of("3 tháng", "6 tháng", "12 tháng");

            return ChatResponseDto.builder()
                    .action(ChatResponseDto.ActionType.SELECT_DURATION)
                    .message("Bạn muốn lộ trình kéo dài bao lâu? Chọn mốc thời gian cố định hoặc nhập cụ thể:")
                    .data(suggestedDurations)
                    .build();
        }

        String combinedNotes = intent.additionalNotes() + ". Thời gian: " + finalDuration;

        RoadmapGenerationDto roadmapJson = creatorAgent.generate(position.getTitle(), combinedNotes);
        saveToDatabase(roadmapJson);
        return ChatResponseDto.builder()
                .action(ChatResponseDto.ActionType.DISPLAY_ROADMAP)
                .message("Tuyệt vời! Tôi đã tạo xong lộ trình " + position.getTitle() + " trong " + finalDuration + " cho bạn.")
                .data(roadmapJson)
                .build();
    }

    private void saveToDatabase(RoadmapGenerationDto roadmapJson) {

    }
}