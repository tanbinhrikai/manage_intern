package com.rikai.backend.service.roadmap;

import com.rikai.backend.ai.dto.request.PositionSelectionDto;
import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.ai.dto.response.ChatResponseDto.ActionType;
import com.rikai.backend.ai.dto.response.UserIntentDto;
import com.rikai.backend.ai.prompt.PromptManager;
import com.rikai.backend.dto.request.agent_ai.ChatAIDto;
import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.model.Enum.DifficultyLevel;
import com.rikai.backend.model.Enum.NodeType;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.RoadmapNode;
import com.rikai.backend.model.Tag;
import com.rikai.backend.repository.InternshipBatchRepository;
import com.rikai.backend.repository.PositionRepository;
import com.rikai.backend.repository.RoadmapNodeRepository;
import com.rikai.backend.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class RoadmapGeneratorService {

    private final ChatClient routerClient;
    private final ChatClient creatorClient;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TagRepository tagRepository;
    private final PositionRepository positionRepository;
    private final InternshipBatchRepository internshipBatchRepository;

    public RoadmapGeneratorService(
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("creatorClient") ChatClient creatorClient,
            RoadmapNodeRepository roadmapNodeRepository,
            TagRepository tagRepository,
            PositionRepository positionRepository,
            InternshipBatchRepository internshipBatchRepository
    ) {
        this.routerClient = routerClient;
        this.creatorClient = creatorClient;
        this.roadmapNodeRepository = roadmapNodeRepository;
        this.tagRepository = tagRepository;
        this.positionRepository = positionRepository;
        this.internshipBatchRepository = internshipBatchRepository;
    }

    public String chatWithAI(ChatAIDto request) {
        return creatorClient
                .prompt(request.message())
                .call()
                .content();
    }

    private UserIntentDto analyzeUserIntent(String userMessage) {
        try {
            return routerClient.prompt() // <--- Dùng Router
                    .user(PromptManager.INTENT_ANALYSIS_SYSTEM.formatted(userMessage))
                    .call()
                    .entity(UserIntentDto.class);
        } catch (Exception e) {
            log.error("AI Analyze Error", e);
            return new UserIntentDto(false, null, null, null, "Xin lỗi, hệ thống đang bận.");
        }
    }
    /**
     * MAIN ENTRY: Xử lý chat thông minh (Router & Slot Filling)
     *
     */
    @Transactional
    public ChatResponseDto processUserMessage(String userMessage, Long positionId, String durationStr, Long batchId) {
        UserIntentDto intent = analyzeUserIntent(userMessage);
        if (!intent.isRoadmapRequest()) {
            return ChatResponseDto.builder()
                    .action(ActionType.NORMAL_CHAT)
                    .message(intent.conversationalReply())
                    .build();
        }

        // --- CASE 2: XỬ LÝ POSITION (Thiếu -> Hiện Dropdown) ---
        Position position = resolvePosition(positionId, intent.detectedTopic());

        if (position == null) {
            // Lấy list position để frontend render dropdown
            List<PositionSelectionDto> allPositions = positionRepository.findAll()
                    .stream().map(p -> new PositionSelectionDto(p.getId(), p.getTitle())).toList();

            return ChatResponseDto.builder()
                    .action(ActionType.SELECT_POSITION)
                    .message("Tôi chưa rõ bạn muốn tạo lộ trình cho vị trí nào. Vui lòng chọn bên dưới hoặc nhập tên:")
                    .data(allPositions)
                    .build();
        }

        // --- CASE 3: XỬ LÝ DURATION (Thiếu -> Hiện Dropdown) ---
        String finalDuration = (durationStr != null && !durationStr.isEmpty())
                ? durationStr
                : intent.detectedDuration();

        if (finalDuration == null || finalDuration.isEmpty()) {
            return ChatResponseDto.builder()
                    .action(ActionType.SELECT_DURATION)
                    .message("Bạn muốn lộ trình học trong bao lâu?")
                    .data(List.of("3 tháng", "6 tháng", "12 tháng"))
                    .build();
        }

        // --- CASE 4: ĐỦ THÔNG TIN -> TẠO LỘ TRÌNH ---
        String combinedNotes = (intent.additionalNotes() != null ? intent.additionalNotes() : "")
                + ". Thời gian: " + finalDuration;

        RoadmapNode createdRoadmap = generateAndSaveRoadmap(position.getTitle(), combinedNotes, position.getId(), batchId);

        return ChatResponseDto.builder()
                .action(ActionType.DISPLAY_ROADMAP)
                .message("Đã tạo xong lộ trình " + position.getTitle() + " (" + finalDuration + ").")
                .data(createdRoadmap)
                .build();
    }

    // --- LOGIC TÌM POSITION TỪ ID HOẶC TỪ KHÓA ---
    private Position resolvePosition(Long inputId, String detectedTopic) {
        if (inputId != null) {
            return positionRepository.findById(inputId).orElse(null);
        }
        if (detectedTopic != null && !detectedTopic.isEmpty()) {
            return positionRepository.findByTitleContainingIgnoreCase(detectedTopic.trim())
                    .stream().findFirst().orElse(null);
        }
        return null;
    }

    @Transactional
    public RoadmapNode generateAndSaveRoadmap(String topic, String notes, Long positionId, Long batchId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new RuntimeException("Position not found: " + positionId));
        InternshipBatch batch = internshipBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found: " + batchId));

        String prompt = PromptManager.ROADMAP_GENERATION_SYSTEM.formatted(topic, notes);

        try {
            RoadmapGenerationDto rootDto = creatorClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(RoadmapGenerationDto.class);
            assert rootDto != null;
            return saveNodeRecursively(rootDto, null, position, batch, 1);
        } catch (Exception e) {
            log.error("Generation Error", e);
            throw new RuntimeException("Lỗi sinh lộ trình: " + e.getMessage());
        }
    }

    private RoadmapNode saveNodeRecursively(RoadmapGenerationDto dto, RoadmapNode parent,
                                            Position position, InternshipBatch batch, int orderIndex) {
        RoadmapNode node = RoadmapNode.builder()
                .title(dto.title())
                .description(dto.description())
                .passCondition(dto.pass_condition())
                .learningOutcome(dto.learning_outcome())
                .estimatedHours(dto.estimated_hours())
                .assessmentMethod(dto.assessment_method())
                .parent(parent)
                .position(position)
                .internshipBatch(batch)
                .orderIndex(orderIndex)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .tags(new HashSet<>())
                .build();

        try {
            node.setNodeType(dto.type() != null ? NodeType.valueOf(dto.type().toUpperCase()) : NodeType.PHASE);
        } catch (Exception e) { node.setNodeType(NodeType.PHASE); }

        try {
            node.setDifficulty(dto.difficulty() != null ? DifficultyLevel.valueOf(dto.difficulty().toUpperCase()) : DifficultyLevel.BEGINNER);
        } catch (Exception e) { node.setDifficulty(DifficultyLevel.BEGINNER); }

        if (dto.tags() != null) {
            Set<Tag> tagEntities = new HashSet<>();
            for (String tagName : dto.tags()) {
                String clean = tagName.trim().toLowerCase();
                if(clean.isEmpty()) continue;
                Tag tag = tagRepository.findByName(clean).orElseGet(() -> {
                    Tag t = new Tag(); t.setName(clean); t.setCreatedAt(LocalDateTime.now());
                    return tagRepository.save(t);
                });
                tagEntities.add(tag);
            }
            node.setTags(tagEntities);
        }

        node = roadmapNodeRepository.save(node);

        if (dto.children() != null) {
            List<RoadmapNode> children = new ArrayList<>();
            int childIdx = 1;
            for (RoadmapGenerationDto child : dto.children()) {
                children.add(saveNodeRecursively(child, node, position, batch, childIdx++));
            }
            node.setChildren(children);
        }
        return node;
    }
}