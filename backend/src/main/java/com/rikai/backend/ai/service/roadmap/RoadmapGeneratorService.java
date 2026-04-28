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
import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.dto.response.roadmap.DraftRoadmapResponseDto;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.model.Enum.DifficultyLevel;
import com.rikai.backend.model.Enum.ExpansionDepth;
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
    private static final double HOURS_TOLERANCE = 0.5;
    private static final Map<String, Double> DURATION_UNIT_MULTIPLIERS = Map.of(
            "tuần", 40.0,
            "week", 40.0,
            "tháng", 80.0,
            "month", 80.0,
            "năm", 960.0,
            "year", 960.0
    );


    public RoadmapGeneratorService(
            IntentRouterAgent intentRouterAgent,
            RoadmapEditorAgent roadmapEditorAgent,
            @Qualifier("routerClient") ChatClient routerClient,
            @Qualifier("creatorClient") ChatClient creatorClient,
            RoadmapNodeRepository roadmapNodeRepository,
            TagRepository tagRepository,
            PositionRepository positionRepository,
            InternshipBatchRepository internshipBatchRepository,
            DraftRoadmapManager draftManager) {
        this.intentRouterAgent = intentRouterAgent;
        this.roadmapEditorAgent = roadmapEditorAgent;
        this.routerClient = routerClient;
        this.creatorClient = creatorClient;
        this.roadmapNodeRepository = roadmapNodeRepository;
        this.tagRepository = tagRepository;
        this.positionRepository = positionRepository;
        this.internshipBatchRepository = internshipBatchRepository;
        this.draftManager = draftManager;
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

    public RoadmapNodeResponse getRoadmapById(Long id) {
        RoadmapNode node = roadmapNodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        return RoadmapNodeResponse.toRoadmapResponse(node);
    }

    @Transactional
    public void deleteRoadmapById(Long id) {
        RoadmapNode node = roadmapNodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        roadmapNodeRepository.delete(node);
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
                    notes != null ? notes : "Not found"
            );
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
                    totalHours
            );
            return new DraftRoadmapResponseDto(
                    sessionId,
                    message,
                    rootNode,
                    DraftRoadmapResponseDto.ActionType.OUTLINE_GENERATED
            );
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
                            DraftRoadmapResponseDto.ActionType.ERROR
                    );
                }
            }
            if (!canExpand(targetNode, depth)) {
                return new DraftRoadmapResponseDto(
                        sessionId,
                        "Cannot expand node this way. " +
                                getExpansionBlockReason(targetNode, depth),
                        rootNode,
                        DraftRoadmapResponseDto.ActionType.ERROR
                );
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
                    targetNode.getTitle(),               // 1. Node title (for task line)
                    targetNode.getNodeType(),            // 2. Node type (for task line)
                    parentContext,                       // 3. Parent Node
                    siblingContext,                      // 4. The content is ALREADY AVAILABLE
                    targetNode.getTitle(),               // 5. Node Title (INPUT PARAMETERS)
                    targetNode.getNodeType(),            // 6. Node Type (INPUT PARAMETERS)
                    parentContext,                       // 7. Parent Context (INPUT PARAMETERS)
                    depth.name(),                        // 8. Expansion Depth
                    targetNode.getEstimatedHours(),      // 9. Estimated hours for this node
                    draft.getDuration(),                 // 10. Total roadmap duration
                    additionalNotes,                     // 11. Additional requirements
                    PromptManager.VALID_TASK_EXAMPLE,    // 12. TASK EXAMPLE
                    PromptManager.NODE_SCHEMA            // 13. SCHEMA FOR EACH NODE
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
                childNode.setOrderIndex(i + 1);
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
                    childrenTotal
            );
            return new DraftRoadmapResponseDto(
                    sessionId,
                    message,
                    rootNode,
                    DraftRoadmapResponseDto.ActionType.NODE_EXPANDED
            );
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
                "thay đổi", "chỉnh sửa", "di chuyển", "move"
        ).anyMatch(lower::contains);
    }

    /**
     * Keyword-based intent analysis (fallback).
     */
    private UserIntentDto analyzeIntentWithKeywords(String message) {
        String lower = message.toLowerCase().trim();
        boolean isRoadmapRequest = Stream.of(
                "tạo lộ trình", "làm roadmap", "muốn học",
                "gợi ý khóa học", "hướng dẫn học", "create roadmap",
                "học như thế nào", "bắt đầu học"
        ).anyMatch(lower::contains);
        if (!isRoadmapRequest) {
            return new UserIntentDto(
                    false,
                    null,
                    null,
                    null,
                    null,
                    "I can help you create a learning plan. What do you want to study?"
            );
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
                null
        );
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
                Map.entry("fullstack", "Full-stack Developer")
        );

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
                Pattern.CASE_INSENSITIVE
        );
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
    private ExpansionDetectionDto detectExpansionRequest(String userMessage, RoadmapNode rootNode, String conversationId) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return new ExpansionDetectionDto(false, null, null, null);
        }
        Pattern expansionPattern = Pattern.compile(
                "(triển khai|expand|mở rộng|chi tiết|tạo).*?" +
                        "(phase|module|lesson|giai đoạn|gđ|mô đun|bài học)\\s*(\\d+(?:\\.\\d+)?)",
                Pattern.CASE_INSENSITIVE
        );
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
                        null
                );
            }
        }
        try {
            List<String> availableNodes = listAvailableNodes(rootNode);
            String availableNodesStr = String.join(", ", availableNodes);

            String prompt = PromptManager.EXPANSION_DETECTION_PROMPT.formatted(
                    userMessage,
                    availableNodesStr
            );
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
                "It's unclear which node you want to deploy. Please be more specific (e.g., 'Deploy PHASE 1.')"
        );
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
     * MAIN ENTRY: Intelligent chat processing (Router & Slot Filling + Expansion Detection + Edit Detection)
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

        log.debug("Processing message - sessionId: {}, conversationId: {}, message: {}", sessionId, convId, userMessage);

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
                            expansionIntent.expansionDepth() != null ?
                                    expansionIntent.expansionDepth() : "FULL_DEPTH"
                    );
                    DraftRoadmapResponseDto result = expandNode(
                            sessionId,
                            expansionIntent.targetNodeTitle(),
                            depth,
                            convId
                    );
                    return ChatResponseDto.builder()
                            .action(ActionType.DISPLAY_ROADMAP)
                            .message(result.message())
                            .data(Map.of(
                                    "sessionId", result.sessionId(),
                                    "roadmapTree", result.roadmapTree()
                            ))
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
                                    "roadmapTree", updatedRoot
                            ))
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
                convId
        );
        return ChatResponseDto.builder()
                .action(ActionType.DISPLAY_ROADMAP)
                .message(outline.message())
                .data(Map.of(
                        "sessionId", outline.sessionId(),
                        "roadmapTree", outline.roadmapTree()
                ))
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
                Pattern.CASE_INSENSITIVE
        );
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
    private RoadmapNode convertDtoToEntity(RoadmapGenerationDto dto, RoadmapNode parent) {
        RoadmapNode node = RoadmapNode.builder()
                .title(dto.title())
                .description(dto.description())
                .passCondition(dto.pass_condition())
                .learningOutcome(dto.learning_outcome())
                .estimatedHours(dto.estimated_hours())
                .assessmentMethod(dto.assessment_method())
                .parent(parent)
                .orderIndex(1)
                .isExpanded(false)
                .tags(new HashSet<>())
                .children(new ArrayList<>())
                .build();
        try {
            node.setNodeType(dto.type() != null ?
                    NodeType.valueOf(dto.type().toUpperCase()) :
                    NodeType.PHASE);
        } catch (Exception e) {
            log.warn("Invalid node type: {}, defaulting to PHASE", dto.type());
            node.setNodeType(NodeType.PHASE);
        }
        try {
            node.setDifficulty(dto.difficulty() != null ?
                    DifficultyLevel.valueOf(dto.difficulty().toUpperCase()) :
                    DifficultyLevel.BEGINNER);
        } catch (Exception e) {
            log.warn("Invalid difficulty: {}, defaulting to BEGINNER", dto.difficulty());
            node.setDifficulty(DifficultyLevel.BEGINNER);
        }
        if (dto.children() != null && !dto.children().isEmpty()) {
            List<RoadmapNode> children = new ArrayList<>();
            for (int i = 0; i < dto.children().size(); i++) {
                RoadmapNode child = convertDtoToEntity(dto.children().get(i), node);
                child.setOrderIndex(i + 1);
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
        if (root == null || targetTitle == null) return null;
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
                if (found != null) return found;
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
                if (found != null) return found;
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
        if (title.startsWith("phase")) return "phase";
        if (title.startsWith("module")) return "module";
        if (title.startsWith("lesson")) return "lesson";
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
        if (node == null) return;
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
                node.getParent().getEstimatedHours()
        );
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
            if (sibling.equals(node)) continue;
            context.append(String.format(
                    "- %s (%.1f hours)%s\n",
                    sibling.getTitle(),
                    sibling.getEstimatedHours() != null ? sibling.getEstimatedHours() : 0,
                    Boolean.TRUE.equals(sibling.getIsExpanded()) ? " [Implemented]" : ""
            ));
        }
        double parentHours = node.getParent().getEstimatedHours() != null ?
                node.getParent().getEstimatedHours() : 0;
        double siblingTotal = node.getParent().getChildren().stream()
                .filter(n -> !n.equals(node))
                .mapToDouble(n -> n.getEstimatedHours() != null ? n.getEstimatedHours() : 0)
                .sum();
        double remainingHours = parentHours - siblingTotal;
        context.append(String.format(
                "\nTime allocated for this node: %.1f hours (out of total %.1f hours of parent)",
                remainingHours,
                parentHours
        ));
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
                    path, parentHours, childrenTotal, Math.abs(parentHours - childrenTotal)
            ));
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
    private RoadmapNode saveNodeTreeRecursively(RoadmapNode node, RoadmapNode parent,
                                                Position position, InternshipBatch batch) {
        node.setPosition(position);
        node.setInternshipBatch(batch);
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
}