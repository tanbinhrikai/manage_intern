package com.rikai.backend.ai.service.roadmap;

import com.rikai.backend.ai.agent.IntentRouterAgent;
import com.rikai.backend.ai.agent.RoadmapEditorAgent;
import com.rikai.backend.ai.dto.response.PositionSelectionDto;
import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.ai.dto.response.ChatResponseDto.ActionType;
import com.rikai.backend.ai.dto.response.ExpansionDetectionDto;
import com.rikai.backend.ai.dto.response.UserIntentDto;
import com.rikai.backend.ai.prompt.PromptManager;
import com.rikai.backend.ai.util.ValidationResult;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.agent_ai.DraftRoadmap;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.AddNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapDto;
import com.rikai.backend.dto.request.roadmap.SaveDraftTreeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapNodeDto;
import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.dto.request.roadmap.AiNodeResponseDto;
import com.rikai.backend.dto.request.roadmap.AiRoadmapDumpDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;
import com.rikai.backend.dto.response.roadmap.DraftRoadmapResponseDto;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.model.Enum.DifficultyLevel;
import com.rikai.backend.model.Enum.ExpansionDepth;
import com.rikai.backend.model.Enum.NodeType;
import com.rikai.backend.model.InternshipBatch;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.Roadmap;
import com.rikai.backend.model.RoadmapNode;
import com.rikai.backend.model.Tag;
import com.rikai.backend.model.Enum.RoadmapStatus;
import com.rikai.backend.repository.InternshipBatchRepository;
import com.rikai.backend.repository.PositionRepository;
import com.rikai.backend.repository.RoadmapRepository;
import com.rikai.backend.repository.RoadmapNodeRepository;
import com.rikai.backend.repository.TagRepository;
import com.rikai.backend.event.RoadmapPublishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import java.util.stream.Collectors;

@Service
@Slf4j
public class RoadmapGeneratorService implements IRoadmapGeneratorService {
    private final IntentRouterAgent intentRouterAgent;
    private final RoadmapEditorAgent roadmapEditorAgent;
    private final ChatClient routerClient;
    private final ChatClient creatorClient;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TagRepository tagRepository;
    private final PositionRepository positionRepository;
    private final InternshipBatchRepository internshipBatchRepository;
    private final DraftRoadmapManager draftManager;
    private final RoadmapRepository roadmapRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DUMP_FILE_PATH = "dummy_data/ai_roadmap_dump.json";
    private static final double HOURS_TOLERANCE = 0.5;
    private static final Map<String, Double> DURATION_UNIT_MULTIPLIERS = Map.of(
            "tuần", 40.0,
            "week", 40.0,
            "tháng", 80.0,
            "month", 80.0,
            "năm", 960.0,
            "year", 960.0);

    public RoadmapGeneratorService(
            IntentRouterAgent intentRouterAgent,
            RoadmapEditorAgent roadmapEditorAgent,
            DraftRoadmapManager draftManager,
            // @Qualifier("roadmapChatClient") ChatClient chatClient,
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("creatorClient") ChatClient creatorClient,
            PositionRepository positionRepository,
            InternshipBatchRepository internshipBatchRepository,
            RoadmapNodeRepository roadmapNodeRepository,
            TagRepository tagRepository,
            RoadmapRepository roadmapRepository,
            ApplicationEventPublisher eventPublisher) {
        this.intentRouterAgent = intentRouterAgent;
        this.roadmapEditorAgent = roadmapEditorAgent;
        this.draftManager = draftManager;
        // this.chatClient = chatClient;
        this.routerClient = routerClient;
        this.creatorClient = creatorClient;
        this.positionRepository = positionRepository;
        this.internshipBatchRepository = internshipBatchRepository;
        this.roadmapNodeRepository = roadmapNodeRepository;
        this.tagRepository = tagRepository;
        this.roadmapRepository = roadmapRepository;
        this.eventPublisher = eventPublisher;
    }

    public PageResponse<RoadmapNodeResponse> getAllRoadmaps(PageRequest pageRequest) {
        Page<RoadmapNode> all = roadmapNodeRepository.findByParentIdIsNull(pageRequest);
        return PageResponse.<RoadmapNodeResponse>builder()
                .items(all.getContent().stream().map(RoadmapNodeResponse::toRoadmapResponse).toList())
                .currentPage(all.getNumber())
                .totalPages(all.getTotalPages())
                .totalItems(all.getTotalElements())
                .pageSize(all.getSize())
                .build();
    }

    @Override
    public RoadmapDto getRoadmapById(Long id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        List<RoadmapNode> rootNodes = roadmapNodeRepository
                .findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(roadmap.getId());
        List<RoadmapNodeDto> nodeDtos = rootNodes.stream().map(this::mapToDto).collect(Collectors.toList());

        return RoadmapDto.builder()
                .id(roadmap.getId())
                .roadmapId(roadmap.getId())
                .title(roadmap.getTitle())
                .description(roadmap.getDescription())
                .durationMonth(roadmap.getDurationMonth())
                .positionId(roadmap.getPosition() != null ? roadmap.getPosition().getId() : null)
                .batchId(roadmap.getInternshipBatch() != null ? roadmap.getInternshipBatch().getId() : null)
                .nodes(nodeDtos)
                .build();
    }

    private RoadmapNodeDto mapToDto(RoadmapNode node) {
        RoadmapNodeDto dto = new RoadmapNodeDto();
        dto.setId(node.getId());
        dto.setTitle(node.getTitle());
        dto.setDescription(node.getDescription());
        dto.setNodeType(node.getNodeType().name());
        dto.setEstimatedHours(node.getEstimatedHours());
        dto.setOrderIndex(node.getOrderIndex());
        dto.setDifficulty(node.getDifficulty() != null ? node.getDifficulty().name() : null);
        dto.setPassCondition(node.getPassCondition());
        dto.setLearningOutcome(node.getLearningOutcome());
        dto.setAssessmentMethod(node.getAssessmentMethod());
        dto.setTags(node.getTags() != null ?
                node.getTags().stream().map(Tag::getName).collect(Collectors.toList()) : null);
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            List<RoadmapNode> sortedChildren = new java.util.ArrayList<>(node.getChildren());
            sortedChildren.sort(java.util.Comparator.comparing(RoadmapNode::getOrderIndex,
                    java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())));
            dto.setChildren(sortedChildren.stream().map(this::mapToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    public List<RoadmapDto> getAllRoadmaps() {
        return roadmapRepository.findAll().stream()
                .map(r -> RoadmapDto.builder()
                        .id(r.getId())
                        .roadmapId(r.getId())
                        .title(r.getTitle())
                        .description(r.getDescription())
                        .durationMonth(r.getDurationMonth())
                        .positionId(r.getPosition() != null ? r.getPosition().getId() : null)
                        .batchId(r.getInternshipBatch() != null ? r.getInternshipBatch().getId() : null)
                        .status(r.getStatus() != null ? r.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRoadmapById(Long id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        roadmapRepository.delete(roadmap);
    }

    /**
     * Generates roadmap OUTLINE only (root + phases).
     * Does NOT save to database - stores in memory as draft.
     */
    public DraftRoadmapResponseDto generateOutline(String topic, String durationStr, String notes,
            Long positionId, Long batchId, String conversationId) {
        try {
            String convId = conversationId != null ? conversationId : UUID.randomUUID().toString();
            double totalHours = convertDurationToHours(durationStr);
            if (totalHours <= 0) {
                throw new RuntimeException("Invalid time : " + durationStr);
            }
            String prompt = PromptManager.ROADMAP_OUTLINE_PROMPT.formatted(
                    topic,
                    String.valueOf(totalHours),
                    notes != null ? notes : "Not found");
            log.info("Generating outline for: {} ({} hours)", topic, totalHours);
            RoadmapGenerationDto rootDto = creatorClient.prompt()
                    .user(prompt)
                    .advisors(a -> a.param(CONVERSATION_ID, convId))
                    .call()
                    .entity(RoadmapGenerationDto.class);

            if (rootDto == null) {
                throw new RuntimeException("LLM does not return valid content.");
            }
            validatePhaseCount(rootDto);
            RoadmapNode rootNode = convertDtoToEntity(rootDto, null);
            ValidationResult validation = validateNodeTree(rootNode);
            if (!validation.valid()) {
                log.warn("Roadmap validation warnings: {}", validation.errors());
            }
            String sessionId = draftManager.createDraft(rootNode, positionId, batchId, durationStr);
            int phaseCount = rootNode.getChildren() != null ? rootNode.getChildren().size() : 0;
            String message = String.format(
                    "Successfully created roadmap outline with %d PHASE(s) (total %s hours).%n" +
                            "You can request detailed expansion for each PHASE.",
                    phaseCount,
                    totalHours);
            return new DraftRoadmapResponseDto(
                    sessionId,
                    message,
                    rootNode,
                    DraftRoadmapResponseDto.ActionType.OUTLINE_GENERATED);
        } catch (Exception e) {
            log.error("Outline Generation Error", e);
            throw new RuntimeException("Error generating roadmap outline: " + e.getMessage());
        }
    }

    // ==================== NODE EXPANSION ====================

    /**
     * Expands a specific node in the draft roadmap.
     */
    public DraftRoadmapResponseDto expandNode(String sessionId, String targetNodeTitle,
            ExpansionDepth depth, String conversationId) {
        try {
            String convId = conversationId != null ? conversationId : UUID.randomUUID().toString();
            DraftRoadmap draft = draftManager.getDraft(sessionId);
            RoadmapNode rootNode = draft.getRootNode();
            RoadmapNode targetNode = findNodeByTitle(rootNode, targetNodeTitle);
            if (targetNode == null) {
                targetNode = findNodeByFuzzyMatch(rootNode, targetNodeTitle);
                if (targetNode == null) {
                    return new DraftRoadmapResponseDto(
                            sessionId,
                            "Node not found: " + targetNodeTitle +
                                    ". Available nodes: " + listAvailableNodes(rootNode),
                            rootNode,
                            DraftRoadmapResponseDto.ActionType.ERROR);
                }
            }
            if (!canExpand(targetNode, depth)) {
                return new DraftRoadmapResponseDto(
                        sessionId,
                        "Cannot expand node this way. " +
                                getExpansionBlockReason(targetNode, depth),
                        rootNode,
                        DraftRoadmapResponseDto.ActionType.ERROR);
            }
            if (targetNode.getChildren() == null) {
                targetNode.setChildren(new ArrayList<>());
            }
            if (Boolean.TRUE.equals(targetNode.getIsExpanded())) {
                log.info("Node {} already expanded, will re-expand", targetNodeTitle);
            }
            String parentContext = buildParentContext(targetNode);
            String siblingContext = buildSiblingContext(targetNode);
            String additionalNotes = draft.getAdditionalNotes() != null ? draft.getAdditionalNotes() : "Not found";

            String prompt = PromptManager.ROADMAP_EXPANSION_PROMPT.formatted(
                    targetNode.getTitle(), // 1. Node title (for task line)
                    targetNode.getNodeType(), // 2. Node type (for task line)
                    parentContext, // 3. Parent Node
                    siblingContext, // 4. The content is ALREADY AVAILABLE
                    targetNode.getTitle(), // 5. Node Title (INPUT PARAMETERS)
                    targetNode.getNodeType(), // 6. Node Type (INPUT PARAMETERS)
                    parentContext, // 7. Parent Context (INPUT PARAMETERS)
                    depth.name(), // 8. Expansion Depth
                    targetNode.getEstimatedHours(), // 9. Estimated hours for this node
                    draft.getDuration(), // 10. Total roadmap duration
                    additionalNotes, // 11. Additional requirements
                    PromptManager.VALID_TASK_EXAMPLE, // 12. TASK EXAMPLE
                    PromptManager.NODE_SCHEMA // 13. SCHEMA FOR EACH NODE
            );
            log.info("Expanding node: {} (depth: {})", targetNodeTitle, depth);
            RoadmapGenerationDto expandedDto = creatorClient.prompt()
                    .user(prompt)
                    .advisors(a -> a.param(CONVERSATION_ID, convId))
                    .call()
                    .entity(RoadmapGenerationDto.class);
            if (expandedDto == null || expandedDto.children() == null) {
                throw new RuntimeException("LLM did not return valid expansion content.");
            }
            List<RoadmapNode> expandedChildren = new ArrayList<>();
            for (int i = 0; i < expandedDto.children().size(); i++) {
                RoadmapGenerationDto childDto = expandedDto.children().get(i);
                RoadmapNode childNode = convertDtoToEntity(childDto, targetNode);
                childNode.setOrderIndex(childDto.order_index() != null ? childDto.order_index() : i + 1);
                expandedChildren.add(childNode);
            }
            targetNode.setChildren(expandedChildren);
            targetNode.setIsExpanded(true);

            double childrenTotal = expandedChildren.stream()
                    .mapToDouble(c -> c.getEstimatedHours() != null ? c.getEstimatedHours() : 0)
                    .sum();
            targetNode.setEstimatedHours(childrenTotal);
            recalculateHierarchyHours(targetNode);
            draftManager.updateDraft(sessionId, rootNode);
            String message = String.format(
                    " Đã triển khai chi tiết cho: %s%n" +
                            "   - Số lượng %s: %d%n" +
                            "   - Tổng giờ: %.1f",
                    targetNodeTitle,
                    getChildTypeName(depth),
                    expandedChildren.size(),
                    childrenTotal);
            return new DraftRoadmapResponseDto(
                    sessionId,
                    message,
                    rootNode,
                    DraftRoadmapResponseDto.ActionType.NODE_EXPANDED);
        } catch (Exception e) {
            log.error("Expansion Error", e);
            throw new RuntimeException("Error expanding node: " + e.getMessage());
        }
    }

    private UserIntentDto analyzeUserIntent(String userMessage, String conversationId) {
        try {
            UserIntentDto llmResult = intentRouterAgent.analyze(userMessage, conversationId);
            if (llmResult != null) {
                return llmResult;
            }
        } catch (Exception e) {
            log.warn("LLM intent analysis failed, using keyword fallback", e);
        }
        return analyzeIntentWithKeywords(userMessage);
    }

    /**
     * Detects if user message is an edit request (add/remove/update node).
     */
    private boolean isEditRequest(String userMessage) {
        String lower = userMessage.toLowerCase().trim();
        return Stream.of(
                "thêm", "xóa", "bỏ", "sửa", "đổi tên", "cập nhật",
                "rename", "add", "remove", "delete", "update",
                "thay đổi", "chỉnh sửa", "di chuyển", "move").anyMatch(lower::contains);
    }

    /**
     * Keyword-based intent analysis (fallback).
     */
    private UserIntentDto analyzeIntentWithKeywords(String message) {
        String lower = message.toLowerCase().trim();
        boolean isRoadmapRequest = Stream.of(
                "tạo lộ trình", "làm roadmap", "muốn học",
                "gợi ý khóa học", "hướng dẫn học", "create roadmap",
                "học như thế nào", "bắt đầu học").anyMatch(lower::contains);
        if (!isRoadmapRequest) {
            return new UserIntentDto(
                    false,
                    null,
                    null,
                    null,
                    null,
                    "I can help you create a learning plan. What do you want to study?");
        }
        String topic = extractTopicKeywords(message);
        String duration = extractDurationPattern(message);
        log.debug("Keyword analysis - topic: {}, duration: {}", topic, duration);
        return new UserIntentDto(
                true,
                topic,
                duration,
                null,
                null,
                null);
    }

    /**
     * Extracts topic from message using keyword matching.
     */
    private String extractTopicKeywords(String message) {
        String lower = message.toLowerCase();
        Map<String, String> techKeywords = Map.ofEntries(
                Map.entry("java", "Java"),
                Map.entry("spring boot", "Spring Boot"),
                Map.entry("react", "React"),
                Map.entry("python", "Python"),
                Map.entry("javascript", "JavaScript"),
                Map.entry("typescript", "TypeScript"),
                Map.entry("nodejs", "Node.js"),
                Map.entry("angular", "Angular"),
                Map.entry("vue", "Vue.js"),
                Map.entry("flutter", "Flutter"),
                Map.entry("react native", "React Native"),
                Map.entry("devops", "DevOps"),
                Map.entry("frontend", "Frontend Developer"),
                Map.entry("backend", "Backend Developer"),
                Map.entry("fullstack", "Full-stack Developer"));

        for (Map.Entry<String, String> entry : techKeywords.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Extracts duration from message using regex.
     */
    private String extractDurationPattern(String message) {
        // Pattern: "3 tháng", "6 months", "420 giờ"
        Pattern pattern = Pattern.compile(
                "(\\d+)\\s*(tuần|tháng|month|năm|year|giờ|hour|weeks?|months?|years?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(0); // Return full match (e.g., "3 tháng")
        }
        return null;
    }

    /**
     * Detects if user message is an expansion request.
     * Uses regex first, then LLM fallback.
     */
    private ExpansionDetectionDto detectExpansionRequest(String userMessage, RoadmapNode rootNode,
            String conversationId) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ExpansionDetectionDto(false, null, null, null);
        }
        Pattern expansionPattern = Pattern.compile(
                "(triển khai|expand|mở rộng|chi tiết|tạo).*?" +
                        "(phase|module|lesson|giai đoạn|gđ|mô đun|bài học)\\s*(\\d+(?:\\.\\d+)?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = expansionPattern.matcher(userMessage);
        if (matcher.find()) {
            String nodeType = matcher.group(2);
            String nodeNumber = matcher.group(3);
            String normalizedType = normalizeNodeType(nodeType);
            String targetTitle = normalizedType + " " + nodeNumber;
            List<String> availableNodes = listAvailableNodes(rootNode);
            boolean exists = availableNodes.stream()
                    .anyMatch(t -> t.toLowerCase().contains(targetTitle.toLowerCase()));
            if (exists) {
                ExpansionDepth depth = inferExpansionDepth(userMessage);
                log.debug("Expansion detected via regex - target: {}, depth: {}", targetTitle, depth);
                return new ExpansionDetectionDto(
                        true,
                        targetTitle,
                        depth.name(),
                        null);
            }
        }
        try {
            List<String> availableNodes = listAvailableNodes(rootNode);
            String availableNodesStr = String.join(", ", availableNodes);

            String prompt = PromptManager.EXPANSION_DETECTION_PROMPT.formatted(
                    userMessage,
                    availableNodesStr);
            ExpansionDetectionDto llmResult = routerClient.prompt()
                    .user(prompt)
                    .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                    .call()
                    .entity(ExpansionDetectionDto.class);
            if (llmResult != null && llmResult.isExpansionRequest()) {
                log.debug("Expansion detected via LLM - target: {}", llmResult.targetNodeTitle());
                return llmResult;
            }
        } catch (Exception e) {
            log.error("Expansion detection error", e);
        }

        return new ExpansionDetectionDto(
                false,
                null,
                null,
                "It's unclear which node you want to deploy. Please be more specific (e.g., 'Deploy PHASE 1.')");
    }

    /**
     * Normalizes node type from various forms.
     */
    private String normalizeNodeType(String nodeType) {
        String lower = nodeType.toLowerCase();
        if (lower.contains("phase") || lower.contains("giai đoạn") || lower.equals("gđ")) {
            return "PHASE";
        }
        if (lower.contains("module") || lower.contains("mô đun")) {
            return "MODULE";
        }
        if (lower.contains("lesson") || lower.contains("bài học")) {
            return "LESSON";
        }
        return "PHASE";
    }

    /**
     * Infers expansion depth from message.
     */
    private ExpansionDepth inferExpansionDepth(String normalizedMessage) {
        String lower = normalizedMessage.toLowerCase();
        if (lower.contains("module") || lower.contains("mô đun") || lower.contains("các chủ đề")) {
            return ExpansionDepth.MODULES_ONLY;
        }
        if (lower.contains("lesson") || lower.contains("bài học")) {
            return ExpansionDepth.LESSONS_ONLY;
        }
        return ExpansionDepth.FULL_DEPTH;
    }

    /**
     * MAIN ENTRY: Intelligent chat processing (Router & Slot Filling + Expansion
     * Detection + Edit Detection)
     */
    public ChatResponseDto processUserMessage(String userMessage, Long positionId, String durationStr,
            Long batchId, String sessionId) {
        return processUserMessage(userMessage, positionId, durationStr, batchId, sessionId, null);
    }

    public ChatResponseDto processUserMessage(String userMessage, Long positionId, String durationStr,
            Long batchId, String sessionId, String conversationId) {
        // Default conversationId if not provided
        String convId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : (sessionId != null ? sessionId : UUID.randomUUID().toString());

        log.debug("Processing message - sessionId: {}, conversationId: {}, message: {}", sessionId, convId,
                userMessage);

        // --- PHASE 1: If draft exists, check for expansion or edit requests ---
        if (sessionId != null && !sessionId.trim().isEmpty() && draftManager.exists(sessionId)) {
            log.info("Draft found! Checking for expansion/edit request");
            DraftRoadmap draft = draftManager.getDraft(sessionId);

            // 1a. Check expansion request
            ExpansionDetectionDto expansionIntent = detectExpansionRequest(userMessage, draft.getRootNode(), convId);
            if (expansionIntent.isExpansionRequest() &&
                    expansionIntent.targetNodeTitle() != null &&
                    !expansionIntent.targetNodeTitle().isEmpty()) {
                try {
                    ExpansionDepth depth = ExpansionDepth.valueOf(
                            expansionIntent.expansionDepth() != null ? expansionIntent.expansionDepth() : "FULL_DEPTH");
                    DraftRoadmapResponseDto result = expandNode(
                            sessionId,
                            expansionIntent.targetNodeTitle(),
                            depth,
                            convId);
                    return ChatResponseDto.builder()
                            .action(ActionType.DISPLAY_ROADMAP)
                            .message(result.message())
                            .data(Map.of(
                                    "sessionId", result.sessionId(),
                                    "roadmapTree", result.roadmapTree()))
                            .build();
                } catch (Exception e) {
                    log.error("Expansion failed", e);
                    return ChatResponseDto.builder()
                            .action(ActionType.NORMAL_CHAT)
                            .message("Lỗi khi triển khai: " + e.getMessage())
                            .build();
                }
            }

            // 1b. Check edit request (thêm/xóa/sửa node)
            if (isEditRequest(userMessage)) {
                try {
                    String editResult = roadmapEditorAgent.processEditRequest(userMessage, sessionId, convId);
                    RoadmapNode updatedRoot = draftManager.getDraft(sessionId).getRootNode();
                    return ChatResponseDto.builder()
                            .action(ActionType.EDIT_ROADMAP)
                            .message(editResult)
                            .data(Map.of(
                                    "sessionId", sessionId,
                                    "roadmapTree", updatedRoot))
                            .build();
                } catch (Exception e) {
                    log.error("Edit failed", e);
                    return ChatResponseDto.builder()
                            .action(ActionType.NORMAL_CHAT)
                            .message("Error during editing: " + e.getMessage())
                            .build();
                }
            }
        }

        // --- PHASE 2: Intent analysis (new roadmap request or normal chat) ---
        UserIntentDto intent = analyzeUserIntent(userMessage, convId);
        if (!intent.isRoadmapRequest()) {
            return ChatResponseDto.builder()
                    .action(ActionType.NORMAL_CHAT)
                    .message(intent.conversationalReply())
                    .build();
        }
        Position position = resolvePosition(positionId, intent.detectedTopic());
        if (position == null) {
            List<PositionSelectionDto> allPositions = positionRepository.findAll()
                    .stream()
                    .map(p -> new PositionSelectionDto(p.getId(), p.getTitle()))
                    .toList();
            return ChatResponseDto.builder()
                    .action(ActionType.SELECT_POSITION)
                    .message("I'm not sure which position you want to create a roadmap for. " +
                            "Please select from the options below:")
                    .data(allPositions)
                    .build();
        }
        Long finalBatchId = batchId;
        if (finalBatchId == null) {
            InternshipBatch activeBatch = internshipBatchRepository.findByStatus_OnGoing()
                    .orElseThrow(() -> new RuntimeException("System has not opened any internship batches!"));
            finalBatchId = activeBatch.getId();
        }
        String finalDuration = (durationStr != null && !durationStr.isEmpty())
                ? durationStr
                : intent.detectedDuration();
        if (finalDuration == null || finalDuration.isEmpty()) {
            return ChatResponseDto.builder()
                    .action(ActionType.SELECT_DURATION)
                    .message("How long do you want your learning roadmap to be?")
                    .data(List.of("3 months (240 hours)", "6 months (480 hours)", "12 months (960 hours)"))
                    .build();
        }
        String combinedNotes = (intent.additionalNotes() != null ? intent.additionalNotes() : "");
        DraftRoadmapResponseDto outline = generateOutline(
                position.getTitle(),
                finalDuration,
                combinedNotes,
                position.getId(),
                finalBatchId,
                convId);
        return ChatResponseDto.builder()
                .action(ActionType.DISPLAY_ROADMAP)
                .message(outline.message())
                .data(Map.of(
                        "sessionId", outline.sessionId(),
                        "roadmapTree", outline.roadmapTree()))
                .build();
    }

    /**
     * Lists all active drafts (for debugging).
     */
    public Map<String, DraftRoadmapManager.DraftInfo> listAllDrafts() {
        return draftManager.getAllDrafts();
    }

    /**
     * Confirms and saves a draft roadmap to database.
     */
    @Transactional
    public RoadmapNode confirmAndSaveDraft(String sessionId) {
        try {
            DraftRoadmap draft = draftManager.getDraft(sessionId);
            Position position = positionRepository.findById(draft.getPositionId())
                    .orElseThrow(() -> new RuntimeException("Position not found: " + draft.getPositionId()));
            InternshipBatch batch = internshipBatchRepository.findById(draft.getBatchId())
                    .orElseThrow(() -> new RuntimeException("Batch not found: " + draft.getBatchId()));
            RoadmapNode rootNode = draft.getRootNode();
            ValidationResult validation = validateNodeTree(rootNode);
            if (!validation.valid()) {
                log.warn("Saving roadmap with validation warnings: {}", validation.errors());
            }
            RoadmapNode savedRoot = saveNodeTreeRecursively(rootNode, null, position, batch);
            draftManager.removeDraft(sessionId);
            log.info("Draft roadmap confirmed and saved. Root ID: {}", savedRoot.getId());
            return savedRoot;

        } catch (Exception e) {
            log.error("Error saving draft roadmap", e);
            throw new RuntimeException("Error saving roadmap: " + e.getMessage());
        }
    }

    /**
     * Cleanup stale drafts periodically.
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void cleanupStaleDrafts() {
        try {
            int removed = draftManager.removeExpiredDrafts(Duration.ofHours(24));
            if (removed > 0) {
                log.info("🧹 Cleaned up {} stale draft sessions", removed);
            }
        } catch (Exception e) {
            log.error("Error cleaning up drafts", e);
        }
    }

    /**
     * Converts duration string to hours.
     */
    private double convertDurationToHours(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            return 0;
        }
        String lower = duration.toLowerCase().trim();
        Pattern pattern = Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s*(tuần|tháng|month|năm|year|giờ|hour|weeks?|months?|years?)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(lower);
        if (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("giờ") || unit.startsWith("hour")) {
                return number;
            }
            for (Map.Entry<String, Double> entry : DURATION_UNIT_MULTIPLIERS.entrySet()) {
                if (unit.startsWith(entry.getKey())) {
                    return number * entry.getValue();
                }
            }
        }
        log.warn("Cannot parse duration: {}, defaulting to 0", duration);
        return 0;
    }

    /**
     * Validates phase count.
     */
    private void validatePhaseCount(RoadmapGenerationDto rootDto) {
        if (rootDto.children() == null || rootDto.children().isEmpty()) {
            throw new RuntimeException("LLM did not create any PHASEs for the roadmap.");
        }

        int phaseCount = rootDto.children().size();

        if (phaseCount < PromptManager.MIN_PHASES) {
            log.warn("LLM only created {} phases (minimum: {})", phaseCount, PromptManager.MIN_PHASES);
        } else if (phaseCount > PromptManager.MAX_PHASES) {
            log.warn("LLM created {} phases (maximum: {})", phaseCount, PromptManager.MAX_PHASES);
        } else {
            log.info(" LLM created {} phases (within acceptable range: {}-{})",
                    phaseCount, PromptManager.MIN_PHASES, PromptManager.MAX_PHASES);
        }
    }

    /**
     * Converts DTO to entity with validation.
     */
    private String cleanTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.replaceAll("(?i)^(Giai đoạn|Phase|Module|Lesson|Topic|Task|Node)\\s*\\d+(\\.\\d+)*\\s*[:\\-\\.]?\\s*", "").trim();
    }

    private RoadmapNode convertDtoToEntity(RoadmapGenerationDto dto, RoadmapNode parent) {
        RoadmapNode node = RoadmapNode.builder()
                .title(cleanTitle(dto.title()))
                .description(dto.description())
                .passCondition(dto.pass_condition())
                .learningOutcome(dto.learning_outcome())
                .estimatedHours(dto.estimated_hours())
                .assessmentMethod(dto.assessment_method())
                .parent(parent)
                .orderIndex(dto.order_index() != null ? dto.order_index() : 1)
                .isExpanded(false)
                .tags(new HashSet<>())
                .children(new ArrayList<>())
                .build();
        try {
            node.setNodeType(dto.type() != null ? NodeType.valueOf(dto.type().toUpperCase()) : NodeType.PHASE);
        } catch (Exception e) {
            log.warn("Invalid node type: {}, defaulting to PHASE", dto.type());
            node.setNodeType(NodeType.PHASE);
        }
        try {
            node.setDifficulty(dto.difficulty() != null ? DifficultyLevel.valueOf(dto.difficulty().toUpperCase())
                    : DifficultyLevel.BEGINNER);
        } catch (Exception e) {
            log.warn("Invalid difficulty: {}, defaulting to BEGINNER", dto.difficulty());
            node.setDifficulty(DifficultyLevel.BEGINNER);
        }
        if (dto.children() != null && !dto.children().isEmpty()) {
            List<RoadmapNode> children = new ArrayList<>();
            for (int i = 0; i < dto.children().size(); i++) {
                RoadmapNode child = convertDtoToEntity(dto.children().get(i), node);
                child.setOrderIndex(dto.children().get(i).order_index() != null ? dto.children().get(i).order_index() : i + 1);
                children.add(child);
            }
            node.setChildren(children);
        }

        return node;
    }

    /**
     * Finds node by exact or fuzzy title match.
     */
    private RoadmapNode findNodeByTitle(RoadmapNode root, String targetTitle) {
        if (root == null || targetTitle == null)
            return null;
        String normalizedTarget = targetTitle.toLowerCase().trim();
        String normalizedNodeTitle = root.getTitle().toLowerCase().trim();
        if (normalizedNodeTitle.equals(normalizedTarget)) {
            return root;
        }
        if (normalizedNodeTitle.startsWith(normalizedTarget)) {
            return root;
        }
        String targetNumber = extractNodeNumber(normalizedTarget);
        String nodeNumber = extractNodeNumber(normalizedNodeTitle);
        if (targetNumber != null && targetNumber.equals(nodeNumber)) {
            String targetType = extractNodeTypePrefix(normalizedTarget);
            String nodeType = extractNodeTypePrefix(normalizedNodeTitle);
            if (targetType != null && targetType.equals(nodeType)) {
                return root;
            }
        }
        if (root.getChildren() != null) {
            for (RoadmapNode child : root.getChildren()) {
                RoadmapNode found = findNodeByTitle(child, targetTitle);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    /**
     * Fuzzy match for when exact match fails.
     */
    private RoadmapNode findNodeByFuzzyMatch(RoadmapNode root, String targetTitle) {
        if (root.getTitle().toLowerCase().contains(targetTitle.toLowerCase())) {
            return root;
        }
        if (root.getChildren() != null) {
            for (RoadmapNode child : root.getChildren()) {
                RoadmapNode found = findNodeByFuzzyMatch(child, targetTitle);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    /**
     * Extracts node number from title (e.g., "1" from "PHASE 1").
     */
    private String extractNodeNumber(String title) {
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(title);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extracts node type prefix (e.g., "phase" from "phase 1: title").
     */
    private String extractNodeTypePrefix(String title) {
        if (title.startsWith("phase"))
            return "phase";
        if (title.startsWith("module"))
            return "module";
        if (title.startsWith("lesson"))
            return "lesson";
        return null;
    }

    /**
     * Lists all available node titles (for error messages).
     */
    private List<String> listAvailableNodes(RoadmapNode root) {
        List<String> titles = new ArrayList<>();
        collectNodeTitles(root, titles);
        return titles;
    }

    private void collectNodeTitles(RoadmapNode node, List<String> titles) {
        if (node == null)
            return;
        titles.add(node.getTitle());
        if (node.getChildren() != null) {
            for (RoadmapNode child : node.getChildren()) {
                collectNodeTitles(child, titles);
            }
        }
    }

    /**
     * Checks if a node can be expanded with given depth.
     */
    private boolean canExpand(RoadmapNode node, ExpansionDepth depth) {
        NodeType type = node.getNodeType();
        if (type == NodeType.PHASE) {
            return true;
        }
        if (type == NodeType.MODULE) {
            return depth != ExpansionDepth.MODULES_ONLY;
        }
        if (type == NodeType.LESSON) {
            return depth == ExpansionDepth.FULL_DEPTH;
        }
        return false;
    }

    /**
     * Gets reason why expansion is blocked.
     */
    private String getExpansionBlockReason(RoadmapNode node, ExpansionDepth depth) {
        NodeType type = node.getNodeType();
        if (type == NodeType.TASK) {
            return "TASK is a leaf node and cannot be further deployed.";
        }
        if (type == NodeType.LESSON && depth != ExpansionDepth.FULL_DEPTH) {
            return "LESSON can only be deployed into TASKs (use FULL_DEPTH).";
        }
        if (type == NodeType.MODULE && depth == ExpansionDepth.MODULES_ONLY) {
            return "MODULE cannot be further deployed into child MODULEs.";
        }
        return "Reason not specified.";
    }

    /**
     * Gets child type name for display.
     */
    private String getChildTypeName(ExpansionDepth depth) {
        return switch (depth) {
            case MODULES_ONLY -> "modules";
            case LESSONS_ONLY -> "lessons";
            case FULL_DEPTH -> "items";
        };
    }

    /**
     * Builds parent context string for LLM.
     */
    private String buildParentContext(RoadmapNode node) {
        if (node.getParent() == null) {
            return "Root Node (no parent)";
        }
        return String.format(
                "Parent: %s (%s, %.1f hours)",
                node.getParent().getTitle(),
                node.getParent().getNodeType(),
                node.getParent().getEstimatedHours());
    }

    /**
     * Builds sibling context string for LLM (with information about hours).
     */
    private String buildSiblingContext(RoadmapNode node) {
        if (node.getParent() == null || node.getParent().getChildren() == null) {
            return "Not sibling nodes";
        }
        StringBuilder context = new StringBuilder();
        context.append("Nodes at the same level:\n");
        for (RoadmapNode sibling : node.getParent().getChildren()) {
            if (sibling.equals(node))
                continue;
            context.append(String.format(
                    "- %s (%.1f hours)%s\n",
                    sibling.getTitle(),
                    sibling.getEstimatedHours() != null ? sibling.getEstimatedHours() : 0,
                    Boolean.TRUE.equals(sibling.getIsExpanded()) ? " [Implemented]" : ""));
        }
        double parentHours = node.getParent().getEstimatedHours() != null ? node.getParent().getEstimatedHours() : 0;
        double siblingTotal = node.getParent().getChildren().stream()
                .filter(n -> !n.equals(node))
                .mapToDouble(n -> n.getEstimatedHours() != null ? n.getEstimatedHours() : 0)
                .sum();
        double remainingHours = parentHours - siblingTotal;
        context.append(String.format(
                "\nTime allocated for this node: %.1f hours (out of total %.1f hours of parent)",
                remainingHours,
                parentHours));
        return context.toString();
    }

    /**
     * Validates entire node tree for hours balance and structure.
     */
    private ValidationResult validateNodeTree(RoadmapNode root) {
        List<String> errors = new ArrayList<>();
        validateNodeHours(root, errors, "ROOT");
        return new ValidationResult(errors.isEmpty(), errors);
    }

    private void validateNodeHours(RoadmapNode node, List<String> errors, String path) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return;
        }
        double parentHours = node.getEstimatedHours() != null ? node.getEstimatedHours() : 0;
        double childrenTotal = node.getChildren().stream()
                .mapToDouble(c -> c.getEstimatedHours() != null ? c.getEstimatedHours() : 0)
                .sum();
        if (Math.abs(parentHours - childrenTotal) > HOURS_TOLERANCE) {
            errors.add(String.format(
                    "%s: Hours mismatch - parent=%.1f, children=%.1f (diff=%.1f)",
                    path, parentHours, childrenTotal, Math.abs(parentHours - childrenTotal)));
        }
        for (int i = 0; i < node.getChildren().size(); i++) {
            RoadmapNode child = node.getChildren().get(i);
            String childPath = path + " → " + child.getTitle();
            validateNodeHours(child, errors, childPath);
        }
    }

    /**
     * Saves node tree to database recursively.
     */

    private RoadmapNode saveNodeTreeRecursively(RoadmapNode node, RoadmapNode parent) {
        node.setParent(parent);
        node.setCreatedAt(Instant.now());
        node.setUpdatedAt(Instant.now());
        if (node.getChildren() == null) {
            node.setChildren(new ArrayList<>());
        }
        if (node.getTags() != null && !node.getTags().isEmpty()) {
            Set<Tag> persistedTags = new HashSet<>();
            for (Tag tag : node.getTags()) {
                Tag persistedTag = tagRepository.findByName(tag.getName())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tag.getName());
                            newTag.setCreatedAt(LocalDateTime.now());
                            return tagRepository.save(newTag);
                        });
                persistedTags.add(persistedTag);
            }
            node.setTags(persistedTags);
        }
        RoadmapNode savedNode = roadmapNodeRepository.save(node);
        if (!node.getChildren().isEmpty()) {
            List<RoadmapNode> savedChildren = new ArrayList<>();
            for (RoadmapNode child : node.getChildren()) {
                child.setRoadmap(node.getRoadmap());
                savedChildren.add(saveNodeTreeRecursively(child, savedNode));
            }
            savedNode.setChildren(savedChildren);
        }

        return savedNode;
    }

    private RoadmapNode saveNodeTreeRecursively(RoadmapNode node, RoadmapNode parent,
            Position position, InternshipBatch batch) {
        node.setParent(parent);
        node.setCreatedAt(Instant.now());
        node.setUpdatedAt(Instant.now());
        if (node.getChildren() == null) {
            node.setChildren(new ArrayList<>());
        }
        if (node.getTags() != null && !node.getTags().isEmpty()) {
            Set<Tag> persistedTags = new HashSet<>();
            for (Tag tag : node.getTags()) {
                Tag persistedTag = tagRepository.findByName(tag.getName())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tag.getName());
                            newTag.setCreatedAt(LocalDateTime.now());
                            return tagRepository.save(newTag);
                        });
                persistedTags.add(persistedTag);
            }
            node.setTags(persistedTags);
        }
        RoadmapNode savedNode = roadmapNodeRepository.save(node);
        if (!node.getChildren().isEmpty()) {
            List<RoadmapNode> savedChildren = new ArrayList<>();
            for (RoadmapNode child : node.getChildren()) {
                child.setRoadmap(node.getRoadmap());
                savedChildren.add(saveNodeTreeRecursively(child, savedNode, position, batch));
            }
            savedNode.setChildren(savedChildren);
        }

        return savedNode;
    }

    /**
     * Resolves Position from ID or detected topic.
     */
    private Position resolvePosition(Long inputId, String detectedTopic) {
        if (inputId != null) {
            return positionRepository.findById(inputId).orElse(null);
        }
        if (detectedTopic != null && !detectedTopic.trim().isEmpty()) {
            String keyword = detectedTopic.trim();
            return positionRepository.findByTitleContainingIgnoreCase(keyword)
                    .stream()
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void recalculateHierarchyHours(RoadmapNode node) {
        RoadmapNode current = node;
        while (current.getParent() != null) {
            RoadmapNode parent = current.getParent();
            double sumSiblings = parent.getChildren().stream()
                    .mapToDouble(n -> n.getEstimatedHours() != null ? n.getEstimatedHours() : 0)
                    .sum();
            parent.setEstimatedHours(sumSiblings);
            current = parent;
        }
    }

    /**
     * Mô phỏng gọi AI: đọc JSON dump và trả về danh sách node theo loại NodeType
     * cần sinh.
     * TODO: Khi tích hợp AI thật, thay toàn bộ phần đọc file bằng HTTP call tới
     * OpenAI/Gemini API.
     * Contract giữ nguyên: nhận NodeType, trả về List<AiNodeResponseDto>.
     */
    private List<AiNodeResponseDto> simulateAiCall(NodeType targetType) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            ClassPathResource resource = new ClassPathResource(DUMP_FILE_PATH);
            AiRoadmapDumpDto dump = objectMapper.readValue(resource.getInputStream(), AiRoadmapDumpDto.class);
            return switch (targetType) {
                case PHASE -> dump.getPHASE_RESPONSE() != null ? dump.getPHASE_RESPONSE() : Collections.emptyList();
                case MODULE -> dump.getMODULE_RESPONSE() != null ? dump.getMODULE_RESPONSE() : Collections.emptyList();
                case LESSON -> dump.getLESSON_RESPONSE() != null ? dump.getLESSON_RESPONSE() : Collections.emptyList();
                case TOPIC -> dump.getTOPIC_RESPONSE() != null ? dump.getTOPIC_RESPONSE() : Collections.emptyList();
                case TASK -> dump.getTASK_RESPONSE() != null ? dump.getTASK_RESPONSE() : Collections.emptyList();
            };
        } catch (Exception e) {
            log.error("Failed to read AI dump file: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Chuyển đổi AiNodeResponseDto sang RoadmapNode entity và lưu vào DB.
     */
    private RoadmapNode buildAndSaveNode(AiNodeResponseDto dto, NodeType nodeType, Roadmap roadmap,
            RoadmapNode parent, int orderIndex) {
        DifficultyLevel difficultyLevel = null;
        if (dto.getDifficulty() != null) {
            try {
                difficultyLevel = DifficultyLevel.valueOf(dto.getDifficulty());
            } catch (Exception ignored) {
            }
        }
        RoadmapNode node = RoadmapNode.builder()
                .roadmap(roadmap)
                .parent(parent)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .nodeType(nodeType)
                .estimatedHours(dto.getEstimatedHours() != null ? dto.getEstimatedHours() : 0.0)
                .difficulty(difficultyLevel)
                .learningOutcome(dto.getLearningOutcome())
                .passCondition(dto.getPassCondition())
                .assessmentMethod(dto.getAssessmentMethod())
                .isExpanded(false)
                .orderIndex(orderIndex)
                .children(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return roadmapNodeRepository.save(node);
    }

    @Override
    @Transactional
    public List<RoadmapNodeResponse> generateMockPhases(GeneratePhasesRequest request) {
        log.info("Generating phases using AI for Position: {}, Batch: {}", request.getPositionId(),
                request.getBatchId());

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new RuntimeException("Position not found: " + request.getPositionId()));
        InternshipBatch batch = internshipBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found: " + request.getBatchId()));

        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(request.getPrompt() != null && !request.getPrompt().isBlank()
                ? request.getPrompt()
                : "AI Roadmap for " + position.getTitle());
        roadmap.setDescription("AI generated roadmap for position: " + position.getTitle());
        roadmap.setDurationMonth(request.getDuration());
        roadmap.setPosition(position);
        roadmap.setInternshipBatch(batch);
        roadmap.setStatus(RoadmapStatus.DRAFT);
        roadmap.setCreatedAt(Instant.now());
        roadmap.setUpdatedAt(Instant.now());
        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        double totalHours = request.getDuration() * 80.0; // 80 hours per month
        String notes = (request.getPrompt() != null && !request.getPrompt().isBlank()) ? request.getPrompt()
                : "Không có yêu cầu đặc biệt";

        String systemPrompt = """
                Bạn là một chuyên gia thiết kế lộ trình học tập và đào tạo lập trình IT cho thực tập sinh.
                Hãy tạo ra khung chương trình (các giai đoạn - PHASE) cho vị trí thực tập sinh: %s với chương trình %s.
                Tổng số giờ đào tạo cho toàn bộ lộ trình phải ĐÚNG BẰNG %s giờ.
                Yêu cầu bổ sung của người dùng: %s

                Yêu cầu cấu trúc:
                - Trả về cấu trúc JSON đại diện cho ROOT node của lộ trình.
                - Trường 'children' phải chứa danh sách các giai đoạn (PHASE) con (từ 4 đến 6 giai đoạn).
                - Các giai đoạn con phải phân bổ số giờ (estimated_hours) sao cho tổng số giờ của tất cả các giai đoạn đúng bằng %s giờ.
                - Mỗi giai đoạn (PHASE) con có các trường: title, description, estimated_hours, difficulty, learning_outcome, pass_condition, assessment_method.
                - Tuyệt đối KHÔNG viết các tiền tố như "Giai đoạn X:", "Phase X:" ở đầu tiêu đề (title). Tiêu đề chỉ chứa tên thực tế của giai đoạn đó.
                - Tuyệt đối KHÔNG tạo thêm các cấp sâu hơn (MODULE, LESSON, TOPIC, TASK) ở bước này.
                - Trả về đúng định dạng JSON khớp với schema sau:
                %s

                Lưu ý: Chỉ trả về JSON thuần túy, không định dạng markdown hay bất kỳ ký tự nào ngoài JSON.
                """;

        String formattedPrompt = String.format(systemPrompt, position.getTitle(),batch.getName(), String.valueOf(totalHours), notes,
                String.valueOf(totalHours), PromptManager.NODE_SCHEMA);

        log.info("Sending outline generation request to Gemini/AI Client for Position: {}", position.getTitle());
        RoadmapGenerationDto rootDto = creatorClient.prompt()
                .user(formattedPrompt)
                .call()
                .entity(RoadmapGenerationDto.class);

        if (rootDto == null || rootDto.children() == null) {
            throw new RuntimeException("AI did not return a valid roadmap outline");
        }

        List<RoadmapNodeResponse> responses = new ArrayList<>();
        for (int i = 0; i < rootDto.children().size(); i++) {
            RoadmapGenerationDto phaseDto = rootDto.children().get(i);
            RoadmapNode phaseNode = convertDtoToEntity(phaseDto, null);
            phaseNode.setRoadmap(savedRoadmap);
            phaseNode.setOrderIndex(phaseDto.order_index() != null ? phaseDto.order_index() : i + 1);

            RoadmapNode saved = roadmapNodeRepository.save(phaseNode);
            responses.add(RoadmapNodeResponse.toRoadmapResponse(saved));
        }

        log.info("Generated {} PHASE nodes from AI", responses.size());
        return responses;
    }
    @Override
    @Transactional
    public NodeExpansionResponse expandMockNode(Long nodeId, String prompt) {
        log.info("Expanding node ID: {} using AI. Prompt: {}", nodeId, prompt);

        RoadmapNode parentNode = roadmapNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parent node not found: " + nodeId));

        NodeType parentType = parentNode.getNodeType();

        // Xác định loại con cần sinh dựa vào loại cha (Lazy Loading: mỗi lần expand chỉ sinh 1 tầng)
        NodeType childType = switch (parentType) {
            case PHASE -> NodeType.MODULE;
            case MODULE -> NodeType.LESSON;
            case LESSON -> NodeType.TOPIC;
            case TOPIC -> NodeType.TASK;
            case TASK -> null; // TASK là node lá, không thể expand
        };

        if (childType == null) {
            log.warn("Node ID {} is of type TASK - cannot expand further", nodeId);
            return NodeExpansionResponse.builder()
                    .parentId(nodeId)
                    .children(Collections.emptyList())
                    .build();
        }

        List<RoadmapNode> existingChildren = parentNode.getChildren() != null 
                ? new ArrayList<>(parentNode.getChildren()) 
                : new ArrayList<>();

        // Caching: Nếu không có prompt điều chỉnh và đã có sẵn node con trong DB, trả về luôn
        if ((prompt == null || prompt.isBlank()) && !existingChildren.isEmpty()) {
            log.info("Loading {} existing children for node ID: {} from DB", existingChildren.size(), nodeId);
            List<RoadmapNodeResponse> childrenResponse = existingChildren.stream()
                    .map(RoadmapNodeResponse::toRoadmapResponse)
                    .toList();
            return NodeExpansionResponse.builder()
                    .parentId(nodeId)
                    .children(childrenResponse)
                    .build();
        }

        String notes = (prompt != null && !prompt.isBlank()) ? prompt : "Không có yêu cầu đặc biệt";
        double parentHours = parentNode.getEstimatedHours() != null ? parentNode.getEstimatedHours() : 1.0;

        // Xây dựng danh sách các node con hiện có để gửi vào Prompt cho AI
        StringBuilder existingChildrenText = new StringBuilder();
        if (!existingChildren.isEmpty()) {
            existingChildrenText.append("Danh sách các node con hiện tại:\n");
            for (RoadmapNode child : existingChildren) {
                existingChildrenText.append(String.format("- ID: %d, Tiêu đề: \"%s\", Mô tả: \"%s\", Số giờ dự kiến: %s giờ\n",
                        child.getId(),
                        child.getTitle(),
                        child.getDescription() != null ? child.getDescription() : "",
                        child.getEstimatedHours() != null ? child.getEstimatedHours() : "1.0"));
            }
        } else {
            existingChildrenText.append("Không có node con hiện tại.");
        }

        String systemPrompt = """
                Bạn là một chuyên gia thiết kế chương trình đào tạo IT.
                Nhiệm vụ của bạn là triển khai chi tiết (sinh các node con) cho node cha sau:
                - Tiêu đề node cha: "%s" (loại node: %s)
                - Mô tả node cha: "%s"
                - Số giờ dự kiến cho node cha: %s giờ
                - Loại node con cần sinh: %s

                Ngữ cảnh hiện tại:
                %s

                Yêu cầu bổ sung/điều chỉnh của người dùng:
                %s

                Yêu cầu cấu trúc và xử lý:
                - Trả về cấu trúc JSON đại diện cho node cha.
                - Trường 'children' phải chứa danh sách các node con (loại node: %s), thông thường từ 2 đến 5 node con.
                - Đọc kỹ yêu cầu bổ sung/điều chỉnh của người dùng:
                  + Nếu người dùng muốn giữ lại hoặc sửa đổi một node con cũ, bạn BẮT BUỘC phải điền chính xác 'id' của node con đó từ "Danh sách các node con hiện tại" vào trường 'id' trong JSON kết quả.
                  + Nếu người dùng muốn thêm node con mới, đặt 'id' là null.
                  + Nếu người dùng muốn xóa hoặc thay thế một node con cũ, đơn giản là KHÔNG đưa node đó (với ID của nó) vào danh sách 'children' trả về.
                - Tổng số giờ (estimated_hours) của tất cả các node con được trả về (cả giữ lại, sửa đổi và thêm mới) phải ĐÚNG BẰNG số giờ của node cha là %s giờ.
                - Mỗi node con phải phân bổ số giờ hợp lý (ước tính theo độ khó và thời gian thực hiện, tối thiểu 1 giờ).
                - Mỗi node con có các trường: id, title, description, estimated_hours, difficulty, tags, learning_outcome, pass_condition, assessment_method.
                - Tuyệt đối KHÔNG viết các tiền tố như "Module X:", "Lesson X:", "Topic X:", "Task X:" ở đầu tiêu đề (title). Tiêu đề chỉ chứa tên thực tế của node con đó.
                - Tuyệt đối KHÔNG sinh thêm các cấp con sâu hơn ở bước này.
                - Trả về đúng định dạng JSON khớp với schema sau:
                %s

                Lưu ý: Chỉ trả về JSON thuần túy, không định dạng markdown hay bất kỳ ký tự nào ngoài JSON.
                """;

        String formattedPrompt = String.format(systemPrompt,
                parentNode.getTitle(),
                parentNode.getNodeType().name(),
                parentNode.getDescription() != null ? parentNode.getDescription() : "",
                parentHours,
                childType.name(),
                existingChildrenText.toString(),
                notes,
                childType.name(),
                parentHours,
                PromptManager.NODE_SCHEMA);

        log.info("Sending node expansion request to Gemini/AI Client for Node ID: {} ({})", nodeId,
                parentNode.getTitle());
        RoadmapGenerationDto expandedDto = creatorClient.prompt()
                .user(formattedPrompt)
                .call()
                .entity(RoadmapGenerationDto.class);

        if (expandedDto == null || expandedDto.children() == null) {
            throw new RuntimeException("AI did not return a valid node expansion");
        }

        // Xác định các ID được AI giữ lại
        Set<Long> keptIds = new HashSet<>();
        for (RoadmapGenerationDto childDto : expandedDto.children()) {
            if (childDto.id() != null) {
                keptIds.add(childDto.id());
            }
        }

        // Tìm các node cũ cần xóa
        List<RoadmapNode> toDelete = new ArrayList<>();
        for (RoadmapNode child : existingChildren) {
            if (!keptIds.contains(child.getId())) {
                toDelete.add(child);
            }
        }

        // Thực hiện xóa các node không được giữ lại
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} nodes that AI decided to remove: {}", toDelete.size(),
                    toDelete.stream().map(RoadmapNode::getId).toList());
            roadmapNodeRepository.deleteAll(toDelete);
            parentNode.getChildren().removeAll(toDelete);
            roadmapNodeRepository.flush();
        }

        List<RoadmapNodeResponse> childrenResponse = new ArrayList<>();
        List<RoadmapNode> updatedChildrenList = new ArrayList<>();

        for (int i = 0; i < expandedDto.children().size(); i++) {
            RoadmapGenerationDto childDto = expandedDto.children().get(i);
            RoadmapNode childNode = null;

            if (childDto.id() != null) {
                final Long targetId = childDto.id();
                RoadmapNode existingNode = existingChildren.stream()
                        .filter(n -> n.getId().equals(targetId))
                        .findFirst()
                        .orElse(null);

                if (existingNode != null) {
                    // Cập nhật thông tin node cũ
                    existingNode.setTitle(cleanTitle(childDto.title()));
                    existingNode.setDescription(childDto.description());
                    existingNode.setEstimatedHours(childDto.estimated_hours());
                    if (childDto.difficulty() != null) {
                        try {
                            existingNode.setDifficulty(DifficultyLevel.valueOf(childDto.difficulty().toUpperCase()));
                        } catch (Exception e) {
                            log.warn("Invalid difficulty: {}, defaulting to BEGINNER", childDto.difficulty());
                            existingNode.setDifficulty(DifficultyLevel.BEGINNER);
                        }
                    }
                    existingNode.setPassCondition(childDto.pass_condition());
                    existingNode.setLearningOutcome(childDto.learning_outcome());
                    existingNode.setAssessmentMethod(childDto.assessment_method());
                    existingNode.setOrderIndex(childDto.order_index() != null ? childDto.order_index() : i + 1);
                    childNode = existingNode;
                }
            }

            if (childNode == null) {
                // Thêm node mới
                childNode = convertDtoToEntity(childDto, parentNode);
                childNode.setRoadmap(parentNode.getRoadmap());
                childNode.setOrderIndex(childDto.order_index() != null ? childDto.order_index() : i + 1);
            }

            RoadmapNode saved = roadmapNodeRepository.save(childNode);
            updatedChildrenList.add(saved);
            childrenResponse.add(RoadmapNodeResponse.toRoadmapResponse(saved));
        }

        parentNode.setChildren(updatedChildrenList);
        roadmapNodeRepository.save(parentNode);

        log.info("Generated {} {} nodes using AI under parent ID {}", childrenResponse.size(), childType, nodeId);
        return NodeExpansionResponse.builder()
                .parentId(nodeId)
                .children(childrenResponse)
                .build();
    }

    @Override
    @Transactional
    public RoadmapNodeResponse addNode(AddNodeRequest request) {
        log.info("Add node under parent: {}", request.getParentId());

        RoadmapNode parent = null;
        Roadmap roadmap = null;

        if (request.getParentId() != null) {
            parent = roadmapNodeRepository.findById(request.getParentId()).orElse(null);
        }

        if (request.getRoadmapId() != null) {
            roadmap = roadmapRepository.findById(request.getRoadmapId()).orElse(null);
        } else if (parent != null) {
            roadmap = parent.getRoadmap();
        }

        NodeType type = NodeType.PHASE;
        if (parent != null) {
            switch (parent.getNodeType()) {
                case PHASE:
                    type = NodeType.MODULE;
                    break;
                case MODULE:
                    type = NodeType.LESSON;
                    break;
                case LESSON:
                    type = NodeType.TOPIC;
                    break;
                case TOPIC:
                    type = NodeType.TASK;
                    break;
                default:
                    type = NodeType.TASK;
                    break;
            }
        }

        RoadmapNode newNode = RoadmapNode.builder()
                .title(request.getTitle() != null && !request.getTitle().isEmpty() ? request.getTitle() : "New Node")
                .description(request.getDescription() != null ? request.getDescription() : "Description here")
                .nodeType(type)
                .estimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 1.0)
                .isExpanded(false)
                .children(new ArrayList<>())
                .parent(parent)
                .roadmap(roadmap)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if (roadmap != null) {
            newNode = roadmapNodeRepository.save(newNode);
        }

        return RoadmapNodeResponse.toRoadmapResponse(newNode);
    }

    @Override
    @Transactional
    public RoadmapNodeResponse editMockNode(Long id, EditNodeRequest request) {
        log.info("Editing node ID: {}", id);

        RoadmapNode node = roadmapNodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found: " + id));

        node.setTitle(request.getTitle());
        node.setDescription(request.getDescription());
        if (request.getEstimatedHours() != null) {
            node.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getNodeType() != null) {
            try {
                NodeType newType = NodeType.valueOf(request.getNodeType().toUpperCase());
                if (newType == NodeType.TASK && node.getChildren() != null && !node.getChildren().isEmpty()) {
                    roadmapNodeRepository.deleteAll(node.getChildren());
                    node.getChildren().clear();
                }
                node.setNodeType(newType);
            } catch (Exception e) {
                log.warn("Invalid nodeType: {}", request.getNodeType());
            }
        }
        node.setUpdatedAt(Instant.now());

        RoadmapNode saved = roadmapNodeRepository.save(node);
        return RoadmapNodeResponse.toRoadmapResponse(saved);
    }

    private void updateNodeTypeRecursively(RoadmapNode node, RoadmapNode parent) {
        NodeType newType = NodeType.PHASE;
        if (parent != null) {
            newType = switch (parent.getNodeType()) {
                case PHASE -> NodeType.MODULE;
                case MODULE -> NodeType.LESSON;
                case LESSON -> NodeType.TOPIC;
                case TOPIC -> NodeType.TASK;
                case TASK -> NodeType.TASK;
            };
        }
        node.setNodeType(newType);
        roadmapNodeRepository.save(node);

        List<RoadmapNode> children = node.getChildren();
        if (children != null) {
            for (RoadmapNode child : children) {
                updateNodeTypeRecursively(child, node);
            }
        }
    }

    @Override
    @Transactional
    public RoadmapNodeResponse moveMockNode(Long id, MoveNodeRequest request) {
        log.info("Moving node ID: {} relative to target node ID: {} with dropType: {}", id,
                request.getTargetNodeId(), request.getDropType());

        RoadmapNode movingNode = roadmapNodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node to move not found: " + id));
        RoadmapNode targetNode = roadmapNodeRepository.findById(request.getTargetNodeId())
                .orElseThrow(() -> new RuntimeException("Target node not found: " + request.getTargetNodeId()));

        RoadmapNode oldParent = movingNode.getParent();
        String dropType = request.getDropType();

        if ("inner".equals(dropType)) {
            movingNode.setParent(targetNode);
            int size = targetNode.getChildren() != null ? targetNode.getChildren().size() : 0;
            movingNode.setOrderIndex(size + 1);
            updateNodeTypeRecursively(movingNode, targetNode);
        } else if ("before".equals(dropType) || "after".equals(dropType)) {
            RoadmapNode targetParent = targetNode.getParent();
            movingNode.setParent(targetParent);

            // Fetch siblings
            List<RoadmapNode> siblings;
            if (targetParent == null) {
                siblings = new ArrayList<>(roadmapNodeRepository
                        .findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(movingNode.getRoadmap().getId()));
            } else {
                siblings = new ArrayList<>(targetParent.getChildren());
            }

            // Remove movingNode from siblings if present to recalculate correctly
            siblings.removeIf(n -> n.getId().equals(movingNode.getId()));

            // Find targetNode index in siblings list
            int targetPos = -1;
            for (int i = 0; i < siblings.size(); i++) {
                if (siblings.get(i).getId().equals(targetNode.getId())) {
                    targetPos = i;
                    break;
                }
            }

            if (targetPos != -1) {
                int insertPos = "before".equals(dropType) ? targetPos : targetPos + 1;
                insertPos = Math.max(0, Math.min(insertPos, siblings.size()));
                siblings.add(insertPos, movingNode);
            } else {
                siblings.add(movingNode);
            }

            // Save all siblings with recomputed orderIndex starting from 1
            for (int i = 0; i < siblings.size(); i++) {
                RoadmapNode nodeToUpdate = siblings.get(i);
                nodeToUpdate.setOrderIndex(i + 1);
                roadmapNodeRepository.save(nodeToUpdate);
            }

            updateNodeTypeRecursively(movingNode, targetParent);
        }

        // Recalculate hours for old parent hierarchy and new parent hierarchy
        if (oldParent != null) {
            recalculateHierarchyHours(oldParent);
        }
        if (movingNode.getParent() != null) {
            recalculateHierarchyHours(movingNode.getParent());
        }

        return RoadmapNodeResponse.toRoadmapResponse(movingNode);
    }

    @Override
    @Transactional
    public void deleteMockNode(Long id) {
        log.info("Deleting node ID: {}", id);
        RoadmapNode node = roadmapNodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found: " + id));
        roadmapNodeRepository.delete(node);
    }

    @Override
    @Transactional
    public RoadmapDto saveDraftTree(SaveDraftTreeRequest request) {
        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new RuntimeException("Position not found: " + request.getPositionId()));
        InternshipBatch batch = internshipBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found: " + request.getBatchId()));

        Roadmap roadmap;
        if (request.getId() != null) {
            roadmap = roadmapRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Roadmap not found: " + request.getId()));

            List<RoadmapNode> rootNodes = roadmapNodeRepository
                    .findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(roadmap.getId());
            roadmapNodeRepository.deleteAll(rootNodes);
        } else {
            roadmap = new Roadmap();
            roadmap.setCreatedAt(Instant.now());
        }

        roadmap.setTitle(request.getTitle() != null ? request.getTitle() : "Draft Roadmap");
        roadmap.setDescription(request.getDescription());
        roadmap.setDurationMonth(request.getDurationMonth());
        roadmap.setPosition(position);
        roadmap.setInternshipBatch(batch);
        
        boolean isPublish = request.getPublish() != null && request.getPublish();
        roadmap.setStatus(isPublish ? RoadmapStatus.PUBLISHED : RoadmapStatus.DRAFT);
        roadmap.setUpdatedAt(Instant.now());

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        List<RoadmapNode> savedNodes = new ArrayList<>();

        if (request.getNodes() != null) {
            for (RoadmapNodeDto rootDto : request.getNodes()) {
                RoadmapNode rootNode = mapToEntity(rootDto);

                // Set roadmap
                rootNode.setRoadmap(savedRoadmap);

                savedNodes.add(saveNodeTreeRecursively(rootNode, null));
            }
        }

        if (isPublish) {
            eventPublisher.publishEvent(new RoadmapPublishedEvent(this, savedRoadmap));
        }

        log.info("Saved roadmap (published={}) with ID {} and {} root nodes", isPublish, savedRoadmap.getId(), savedNodes.size());
        return RoadmapDto.builder()
                .id(savedRoadmap.getId())
                .roadmapId(savedRoadmap.getId())
                .title(savedRoadmap.getTitle())
                .description(savedRoadmap.getDescription())
                .durationMonth(savedRoadmap.getDurationMonth())
                .build();
    }

    private RoadmapNode mapToEntity(RoadmapNodeDto dto) {
        RoadmapNode node = new RoadmapNode();
        node.setId(dto.getId());
        node.setTitle(dto.getTitle());
        node.setDescription(dto.getDescription());
        node.setNodeType(dto.getNodeType() != null ? NodeType.valueOf(dto.getNodeType()) : NodeType.PHASE);
        node.setEstimatedHours(dto.getEstimatedHours() != null ? dto.getEstimatedHours() : 0.0);
        node.setOrderIndex(dto.getOrderIndex());
        if (dto.getDifficulty() != null) {
            try {
                node.setDifficulty(DifficultyLevel.valueOf(dto.getDifficulty().toUpperCase()));
            } catch (Exception e) {
                node.setDifficulty(DifficultyLevel.BEGINNER);
            }
        }
        node.setPassCondition(dto.getPassCondition());
        node.setLearningOutcome(dto.getLearningOutcome());
        node.setAssessmentMethod(dto.getAssessmentMethod());
        node.setTags(new HashSet<>());
        if (dto.getTags() != null) {
            for (String tagName : dto.getTags()) {
                Tag tag = new Tag();
                tag.setName(tagName);
                node.getTags().add(tag);
            }
        }
        node.setChildren(new ArrayList<>());

        if (dto.getChildren() != null) {
            for (RoadmapNodeDto childDto : dto.getChildren()) {
                RoadmapNode child = mapToEntity(childDto);
                node.getChildren().add(child);
            }
        }
        return node;
    }
}