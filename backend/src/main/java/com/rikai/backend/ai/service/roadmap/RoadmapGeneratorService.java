package com.rikai.backend.ai.service.roadmap;

import com.rikai.backend.ai.prompt.PromptManager;
import com.rikai.backend.ai.util.ValidationResult;
import com.rikai.backend.dto.request.agent_ai.DraftRoadmap;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapGenerationDto;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;
import com.rikai.backend.dto.response.roadmap.RoadmapResponse;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.model.Enum.DifficultyLevel;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

@Service
@Slf4j
public class RoadmapGeneratorService implements IRoadmapGeneratorService {
    private final ChatClient creatorClient;
    private final RoadmapNodeRepository roadmapNodeRepository;
    private final TagRepository tagRepository;
    private final PositionRepository positionRepository;
    private final InternshipBatchRepository internshipBatchRepository;
    private final DraftRoadmapManager draftManager;
    private final RoadmapRepository roadmapRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final double HOURS_TOLERANCE = 0.5;

    public RoadmapGeneratorService(
            DraftRoadmapManager draftManager,
            @Qualifier("creatorClient") ChatClient creatorClient,
            PositionRepository positionRepository,
            InternshipBatchRepository internshipBatchRepository,
            RoadmapNodeRepository roadmapNodeRepository,
            TagRepository tagRepository,
            RoadmapRepository roadmapRepository,
            ApplicationEventPublisher eventPublisher) {
        this.draftManager = draftManager;
        this.creatorClient = creatorClient;
        this.positionRepository = positionRepository;
        this.internshipBatchRepository = internshipBatchRepository;
        this.roadmapNodeRepository = roadmapNodeRepository;
        this.tagRepository = tagRepository;
        this.roadmapRepository = roadmapRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<RoadmapResponse> getAllRoadmaps() {
        return roadmapRepository.findAll().stream()
                .map(r -> RoadmapResponse.builder()
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

    @Override
    public RoadmapResponse getRoadmapById(Long id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        List<RoadmapNode> rootNodes = roadmapNodeRepository
                .findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(roadmap.getId());
        List<RoadmapNodeResponse> nodeDtos = rootNodes.stream().map(RoadmapNodeResponse::toRoadmapResponse).collect(Collectors.toList());

        return RoadmapResponse.builder()
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

    @Override
    public boolean deleteRoadmapById(Long id) {
        Roadmap roadmap = roadmapRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roadmap not found: " + id));
        try {
            roadmapRepository.delete(roadmap);
            return true;
        } catch (Exception e) {
            log.error("Error deleting roadmap: " + id, e);
            throw new RuntimeException("Error deleting roadmap: " + e.getMessage());
        }
    }

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

    @Override
    @Transactional
    public List<RoadmapNodeResponse> generateMockPhases(GeneratePhasesRequest request) {
        log.info("Generating/Updating phases using AI for Position: {}, Batch: {}", request.getPositionId(),
                request.getBatchId());

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new RuntimeException("Position not found: " + request.getPositionId()));
        InternshipBatch batch = internshipBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found: " + request.getBatchId()));

        Roadmap roadmap;
        List<RoadmapNode> existingPhases = new ArrayList<>();
        if (request.getId() != null) {
            roadmap = roadmapRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Roadmap not found: " + request.getId()));
            existingPhases = roadmapNodeRepository.findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(roadmap.getId());
        } else {
            roadmap = new Roadmap();
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
            roadmap = roadmapRepository.save(roadmap);
        }
        Roadmap savedRoadmap = roadmap;

        double totalHours = request.getDuration() * 80.0; // 80 hours per month
        String notes = (request.getPrompt() != null && !request.getPrompt().isBlank()) ? request.getPrompt()
                : "Không có yêu cầu đặc biệt";

        StringBuilder existingPhasesText = new StringBuilder();
        if (!existingPhases.isEmpty()) {
            existingPhasesText.append("Danh sách các phase (root nodes) hiện tại:\n");
            for (RoadmapNode phase : existingPhases) {
                existingPhasesText.append(String.format("- ID: %d, Tiêu đề: \"%s\", Mô tả: \"%s\", Số giờ dự kiến: %s giờ\n",
                        phase.getId(),
                        phase.getTitle(),
                        phase.getDescription() != null ? phase.getDescription() : "",
                        phase.getEstimatedHours() != null ? phase.getEstimatedHours() : "0"));
            }
        } else {
            existingPhasesText.append("Không có phase hiện tại.");
        }

        String systemPrompt = """
                Bạn là một chuyên gia thiết kế lộ trình thực tập và đào tạo lập trình IT chuyên nghiệp cho thực tập sinh (Intern).
                Hãy tạo ra khung chương trình gồm các giai đoạn (PHASE) đào tạo cho vị trí thực tập sinh: %s thuộc chương trình %s.
                Tổng số giờ đào tạo cho toàn bộ lộ trình phải ĐÚNG BẰNG %s giờ.
                
                ĐỊNH HƯỚNG QUAN TRỌNG:
                - Đối tượng là Thực tập sinh đã có kiến thức nền tảng lập trình cơ bản. Lộ trình KHÔNG thiết kế dạng khóa học dạy lý thuyết cơ bản (như cú pháp ngôn ngữ, biến, vòng lặp...).
                - Lộ trình phải tập trung vào quy trình làm việc thực tế, thực hành dự án và chuẩn bị để hòa nhập vào công việc thực tế của công ty.
                - Khung chương trình chuẩn (các giai đoạn - PHASE) nên bao gồm các giai đoạn đặc thù sau (phân bổ thời gian hợp lý cho từng giai đoạn):
                  1. Onboarding (Hội nhập): Làm quen đội ngũ, tìm hiểu quy trình làm việc (Agile/Scrum, Git Flow), thiết lập môi trường phát triển (Setup IDE, Database, SDK, chạy codebase dự án hiện có).
                  2. Training / Technical Alignment (Đào tạo & Đồng bộ kỹ thuật): Bổ túc và đồng bộ kiến thức chuyên sâu nâng cao (Advanced Framework, State Management, Code Convention, Unit Test, Best Practices, Security) qua các bài thực hành nhỏ (Mini-tasks).
                  3. Project / Practical Experience (Tham gia dự án): Trải nghiệm dự án thực tế (Mock Project hoặc Real Project), nhận task (Jira/Trello), code review, tối ưu hóa hiệu năng, phối hợp nhóm.
                  4. Evaluation & Offboarding (Đánh giá & Tổng kết): Báo cáo kết quả thực tập, đánh giá năng lực toàn diện (Tech & Soft skills), phản hồi hai chiều và chuẩn bị đề xuất lên nhân viên chính thức (Offer review).
                
                Ngữ cảnh các phase hiện tại:
                %s

                Yêu cầu bổ sung/điều chỉnh của người dùng: %s

                Yêu cầu cấu trúc và xử lý:
                - Trả về cấu trúc JSON đại diện cho ROOT node của lộ trình.
                - Trường 'children' phải chứa danh sách các giai đoạn (PHASE) con (từ 4 đến 6 giai đoạn).
                - Các giai đoạn con phải phân bổ số giờ (estimated_hours) sao cho tổng số giờ của tất cả các giai đoạn đúng bằng %s giờ.
                - Đọc kỹ yêu cầu bổ sung/điều chỉnh của người dùng:
                  + Nếu người dùng muốn giữ lại hoặc sửa đổi một phase cũ, bạn BẮT BUỘC phải điền chính xác 'id' của phase cũ đó từ "Danh sách các phase (root nodes) hiện tại" vào trường 'id' trong JSON kết quả.
                  + Nếu người dùng muốn thêm phase mới, đặt 'id' là null.
                  + Nếu người dùng muốn xóa hoặc thay thế một phase cũ, đơn giản là KHÔNG đưa phase đó (với ID của nó) vào danh sách 'children' trả về.
                - Mỗi giai đoạn (PHASE) con có các trường: id, title, description, estimated_hours, difficulty, learning_outcome, pass_condition, assessment_method.
                - Tuyệt đối KHÔNG viết các tiền tố như "Giai đoạn X:", "Phase X:" ở đầu tiêu đề (title). Tiêu đề chỉ chứa tên thực tế của giai đoạn đó.
                - Tuyệt đối KHÔNG tạo thêm các cấp sâu hơn (MODULE, LESSON, TOPIC, TASK) ở bước này.
                - Trả về đúng định dạng JSON khớp với schema sau:
                %s

                Lưu ý: Chỉ trả về JSON thuần túy, không định dạng markdown hay bất kỳ ký tự nào ngoài JSON.
                """;

        String formattedPrompt = String.format(systemPrompt, position.getTitle(), batch.getName(), String.valueOf(totalHours),
                existingPhasesText.toString(), notes, String.valueOf(totalHours), PromptManager.NODE_SCHEMA);

        log.info("Sending outline generation request to Gemini/AI Client for Position: {}", position.getTitle());
        RoadmapGenerationDto rootDto = creatorClient.prompt()
                .user(formattedPrompt)
                .call()
                .entity(RoadmapGenerationDto.class);

        if (rootDto == null || rootDto.children() == null) {
            throw new RuntimeException("AI did not return a valid roadmap outline");
        }

        // Nếu đang chỉnh sửa/regenerate các phase cũ
        if (request.getId() != null) {
            // Xác định các ID được AI giữ lại
            Set<Long> keptIds = new HashSet<>();
            for (RoadmapGenerationDto phaseDto : rootDto.children()) {
                if (phaseDto.id() != null) {
                    keptIds.add(phaseDto.id());
                }
            }

            // Tìm các phase cũ cần xóa
            List<RoadmapNode> toDelete = new ArrayList<>();
            for (RoadmapNode oldPhase : existingPhases) {
                if (!keptIds.contains(oldPhase.getId())) {
                    toDelete.add(oldPhase);
                }
            }

            // Thực hiện xóa các phase không được giữ lại
            if (!toDelete.isEmpty()) {
                log.info("Deleting {} PHASE nodes that AI decided to remove", toDelete.size());
                roadmapNodeRepository.deleteAll(toDelete);
                roadmapNodeRepository.flush();
            }
        }

        List<RoadmapNodeResponse> responses = new ArrayList<>();
        for (int i = 0; i < rootDto.children().size(); i++) {
            RoadmapGenerationDto phaseDto = rootDto.children().get(i);
            RoadmapNode phaseNode = null;

            if (phaseDto.id() != null && request.getId() != null) {
                final Long targetId = phaseDto.id();
                RoadmapNode existingNode = existingPhases.stream()
                        .filter(n -> n.getId().equals(targetId))
                        .findFirst()
                        .orElse(null);

                if (existingNode != null) {
                    // Cập nhật thông tin phase cũ
                    existingNode.setTitle(cleanTitle(phaseDto.title()));
                    existingNode.setDescription(phaseDto.description());
                    existingNode.setEstimatedHours(phaseDto.estimated_hours());
                    if (phaseDto.difficulty() != null) {
                        try {
                            existingNode.setDifficulty(DifficultyLevel.valueOf(phaseDto.difficulty().toUpperCase()));
                        } catch (Exception e) {
                            existingNode.setDifficulty(DifficultyLevel.BEGINNER);
                        }
                    }
                    existingNode.setPassCondition(phaseDto.pass_condition());
                    existingNode.setLearningOutcome(phaseDto.learning_outcome());
                    existingNode.setAssessmentMethod(phaseDto.assessment_method());
                    existingNode.setOrderIndex(phaseDto.order_index() != null ? phaseDto.order_index() : i + 1);
                    phaseNode = existingNode;
                }
            }

            if (phaseNode == null) {
                // Tạo mới phase node
                phaseNode = convertDtoToEntity(phaseDto, null);
                phaseNode.setRoadmap(savedRoadmap);
                phaseNode.setOrderIndex(phaseDto.order_index() != null ? phaseDto.order_index() : i + 1);
            }

            RoadmapNode saved = roadmapNodeRepository.save(phaseNode);
            responses.add(RoadmapNodeResponse.toRoadmapResponse(saved));
        }

        log.info("Generated/Updated {} PHASE nodes from AI", responses.size());
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
                Bạn là một chuyên gia thiết kế chương trình đào tạo IT thực hành cho thực tập sinh (Intern).
                Nhiệm vụ của bạn là triển khai chi tiết (sinh các node con) cho node cha sau:
                - Tiêu đề node cha: "%s" (loại node: %s)
                - Mô tả node cha: "%s"
                - Số giờ dự kiến cho node cha: %s giờ
                - Loại node con cần sinh: %s

                ĐỊNH HƯỚNG QUAN TRỌNG CHO THỰC TẬP SINH:
                - Các nội dung con được sinh ra phải bám sát định hướng thực hành công việc thực tế trong doanh nghiệp, tuyệt đối không dạy các kiến thức lý thuyết cơ bản hàn lâm.
                - Nếu thuộc giai đoạn Onboarding: Tập trung vào các nhiệm vụ như đọc tài liệu phân tích nghiệp vụ, setup môi trường phát triển (cài đặt SDK, DB local), làm quen với quy trình Git (Git Flow), các công cụ quản lý dự án (Jira/Scrum) của công ty.
                - Nếu thuộc giai đoạn Training: Tập trung vào giải quyết các bài tập giả lập (Mini-tasks) để thực hành kỹ năng áp dụng Best Practices, Code Convention, Unit Testing, kiến trúc nâng cao của dự án.
                - Nếu thuộc giai đoạn Project: Thiết kế các task giống như trong công việc thực tế, ví dụ: 'Phát triển chức năng X sử dụng thư viện Y', 'Fix bug Z', 'Tối ưu hóa hiệu năng câu lệnh SQL/API', 'Thực hiện Code Review theo hướng dẫn',...
                - Nếu thuộc giai đoạn Evaluation: Thiết kế các hoạt động đánh giá thực tế như Demo sản phẩm trước hội đồng, viết tài liệu kỹ thuật, Technical Interview ôn tập kiến thức chuyên môn,...

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
    public RoadmapNodeResponse addNode(RoadmapNodeRequest request) {
        log.info("Add node under parent: {}", request.getParentId());

        RoadmapNode parent = null;
        Roadmap roadmap = null;
        
        // Nếu có parentId -> là add node con của node cha đó
        if (request.getParentId() != null) {
            parent = roadmapNodeRepository.findById(request.getParentId()).orElse(null);
            if (parent != null) {
                roadmap = parent.getRoadmap();
            }
        } else if (request.getRoadmapId() != null) {
            // Nếu không có parentId và có roadmapId -> là add root node của roadmap
            roadmap = roadmapRepository.findById(request.getRoadmapId()).orElse(null);
        }

        // Tìm loại node type cho node mới
        NodeType type = (parent == null) ? NodeType.PHASE : switch (parent.getNodeType()) {
            case PHASE -> NodeType.MODULE;
            case MODULE -> NodeType.LESSON;
            case LESSON -> NodeType.TOPIC;
            case TOPIC, TASK -> NodeType.TASK;
        };

        // Tính toán orderIndex tiếp theo (1-based index)
        int nextOrderIndex = 1;
        if (parent != null) {
            nextOrderIndex = (int) roadmapNodeRepository.countByParentId(parent.getId()) + 1;
        } else if (roadmap != null) {
            nextOrderIndex = (int) roadmapNodeRepository.countByRoadmapIdAndParentIsNull(roadmap.getId()) + 1;
        }

        RoadmapNode newNode = RoadmapNode.builder()
                .title(request.getTitle() != null && !request.getTitle().isEmpty() ? request.getTitle() : "New Node")
                .description(request.getDescription() != null ? request.getDescription() : "Description here")
                .nodeType(type)
                .estimatedHours(request.getEstimatedHours() != null ? request.getEstimatedHours() : 1.0)
                .orderIndex(nextOrderIndex)
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
    public RoadmapResponse saveRoadmap(RoadmapRequest request) {
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
            for (RoadmapNodeRequest rootDto : request.getNodes()) {
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
        return RoadmapResponse.builder()
                .id(savedRoadmap.getId())
                .roadmapId(savedRoadmap.getId())
                .title(savedRoadmap.getTitle())
                .description(savedRoadmap.getDescription())
                .durationMonth(savedRoadmap.getDurationMonth())
                .build();
    }

    private RoadmapNode mapToEntity(RoadmapNodeRequest dto) {
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
            for (RoadmapNodeRequest childDto : dto.getChildren()) {
                RoadmapNode child = mapToEntity(childDto);
                node.getChildren().add(child);
            }
        }
        return node;
    }
}