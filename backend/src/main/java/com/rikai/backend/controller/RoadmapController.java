package com.rikai.backend.controller;

import com.rikai.backend.ai.agent.RoadmapEditorAgent;
import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.agent_ai.ChatRequestDto;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.NodeExpansionRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapDto;
import com.rikai.backend.dto.request.roadmap.AddNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.SaveDraftTreeRequest;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;
import com.rikai.backend.dto.response.roadmap.DraftRoadmapResponseDto;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.model.RoadmapNode;
import com.rikai.backend.ai.service.roadmap.IRoadmapGeneratorService;
import com.rikai.backend.ai.service.roadmap.DraftRoadmapManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roadmaps")
@RequiredArgsConstructor
@Slf4j
public class RoadmapController {
    private final IRoadmapGeneratorService roadmapGeneratorService;
    private final RoadmapEditorAgent roadmapEditorAgent;
    private final DraftRoadmapManager draftRoadmapManager;

    /**
     * Main API: Handles chat, creates draft routes, or extends nodes.
     * Automatically detects intent and processing flow (Expansion vs Creation).
     */
    @PostMapping("/chat-process")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<ChatResponseDto> processMessage(@RequestBody ChatRequestDto request) {
        log.info("Chat Process - PosID: {}, Duration: {}, Session: {}, ConvID: {}",
                request.getPositionId(), request.getDuration(),
                request.getSessionId(), request.getConversationId());
        ChatResponseDto response = roadmapGeneratorService.processUserMessage(
                request.getMessage(),
                request.getPositionId(),
                request.getDuration(),
                request.getBatchId(),
                request.getSessionId(),
                request.getConversationId());
        return ApiResponse.buildSuccessResponse(response, SuccessCode.CHAT_PROCESS_SUCCESSFUL);
    }

    /**
     * Confirm saving the draft to the official database.
     */
    @PostMapping("/drafts/{sessionId}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapNodeResponse> confirmDraftRoadmap(@PathVariable String sessionId) {
        RoadmapNode savedRoadmap = roadmapGeneratorService.confirmAndSaveDraft(sessionId);
        RoadmapNodeResponse response = RoadmapNodeResponse.toRoadmapResponse(savedRoadmap);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
    }

    /**
     * Edit a draft roadmap via natural language.
     * The AI will call tools (addNode, removeNode, updateNode) automatically.
     */
    @PostMapping("/drafts/{sessionId}/edit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<ChatResponseDto> editDraftRoadmap(
            @PathVariable String sessionId,
            @RequestBody ChatRequestDto request) {
        if (!draftRoadmapManager.exists(sessionId)) {
            throw new RuntimeException("Draft not found or expired: " + sessionId);
        }
        log.info("Edit Draft - Session: {}, Message: {}", sessionId, request.getMessage());
        String editResult = roadmapEditorAgent.processEditRequest(
                request.getMessage(), sessionId,
                request.getConversationId() != null ? request.getConversationId() : sessionId);
        RoadmapNode updatedRoot = draftRoadmapManager.getDraft(sessionId).getRootNode();
        ChatResponseDto response = ChatResponseDto.builder()
                .action(ChatResponseDto.ActionType.EDIT_ROADMAP)
                .message(editResult)
                .data(java.util.Map.of(
                        "sessionId", sessionId,
                        "roadmapTree", updatedRoot))
                .build();
        return ApiResponse.buildSuccessResponse(response, SuccessCode.CHAT_PROCESS_SUCCESSFUL);
    }

    /**
     * API ADMIN: Quickly create routes (Skip the chat step).
     * Logic: Call to create Draft -> Get SessionId -> Call Confirm immediately.
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapNodeResponse> generateRoadmapQuickly(
            @RequestParam String topic,
            @RequestParam Long positionId,
            @RequestParam Long batchId,
            @RequestParam(required = false, defaultValue = "3 tháng") String duration) {
        log.info("Admin Quick Generate: Topic={}, Duration={}", topic, duration);
        DraftRoadmapResponseDto draft = roadmapGeneratorService.generateOutline(
                topic, duration, "Admin generated via Quick Tool", positionId, batchId, null);
        try {
            RoadmapNode savedRoadmap = roadmapGeneratorService.confirmAndSaveDraft(draft.sessionId());
            RoadmapNodeResponse response = RoadmapNodeResponse.toRoadmapResponse(savedRoadmap);
            return ApiResponse.buildSuccessResponse(response, SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
        } catch (Exception e) {
            // Cleanup draft if save fails
            draftRoadmapManager.removeDraft(draft.sessionId());
            throw e;
        }
    }

    /**
     * Debug: View a list of existing drafts in RAM.
     */
    @GetMapping("/drafts/debug")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> listAllDrafts() {
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.listAllDrafts(),
                SuccessCode.GET_SUCCESSFUL_DRAFTS);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<List<RoadmapDto>> getAllRoadmaps() {
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.getAllRoadmaps(),
                SuccessCode.GET_ALL_ROADMAPS_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapDto> getRoadmapById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.getRoadmapById(id),
                SuccessCode.GET_ROADMAP_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<?> deleteRoadmapById(@PathVariable Long id) {
        roadmapGeneratorService.deleteRoadmapById(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_ROADMAP_SUCCESSFUL);
    }

    @PostMapping("/generate-phases")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<List<RoadmapNodeResponse>> generatePhases(@RequestBody GeneratePhasesRequest request) {
        log.info("API Generate Phases for position: {}", request.getPositionId());
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.generateMockPhases(request),
                SuccessCode.CREATE_ROADMAP_SUCCESSFUL // We reuse success code
        );
    }

    @PostMapping("/nodes/expand")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<NodeExpansionResponse> expandNode(@RequestBody NodeExpansionRequest request) {
        log.info("API Expand Node ID: {}", request.getNodeId());
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.expandMockNode(request.getNodeId(), request.getPrompt()),
                SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
    }

    @PostMapping("/nodes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapNodeResponse> addNode(@RequestBody AddNodeRequest request) {
        log.info("API Add Node under parent: {}", request.getParentId());
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.addNode(request),
                SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
    }

    @PutMapping("/nodes/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapNodeResponse> editNode(@PathVariable Long id, @RequestBody EditNodeRequest request) {
        log.info("API Edit Node ID: {}", id);
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.editMockNode(id, request),
                SuccessCode.UPDATE_ROADMAP_SUCCESSFUL);
    }

    @DeleteMapping("/nodes/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<?> deleteNode(@PathVariable Long id) {
        log.info("API Delete Node ID: {}", id);
        roadmapGeneratorService.deleteMockNode(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_ROADMAP_SUCCESSFUL);
    }

    @PatchMapping("/nodes/{id}/move")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapNodeResponse> moveNode(@PathVariable Long id, @RequestBody MoveNodeRequest request) {
        log.info("API Move Node ID: {}", id);
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.moveMockNode(id, request),
                SuccessCode.UPDATE_ROADMAP_SUCCESSFUL);
    }

    @PostMapping("/drafts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<?> saveDraftRoadmap(@RequestBody SaveDraftTreeRequest request) {
        log.info("API Save Draft Roadmap Tree for position: {} batch: {}", request.getPositionId(),
                request.getBatchId());
        return ApiResponse.buildSuccessResponse(roadmapGeneratorService.saveDraftTree(request),
                SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
    }
}