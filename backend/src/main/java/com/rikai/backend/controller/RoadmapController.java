package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.NodeExpansionRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapRequest;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;
import com.rikai.backend.dto.response.roadmap.RoadmapResponse;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.ai.service.roadmap.IRoadmapGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roadmaps")
@RequiredArgsConstructor
@Slf4j
public class RoadmapController {
    private final IRoadmapGeneratorService roadmapGeneratorService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<List<RoadmapResponse>> getAllRoadmaps() {
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.getAllRoadmaps(),
                SuccessCode.GET_ALL_ROADMAPS_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<RoadmapResponse> getRoadmapById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(
                roadmapGeneratorService.getRoadmapById(id),
                SuccessCode.GET_ROADMAP_SUCCESSFUL);
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
    public ApiResponse<RoadmapNodeResponse> addNode(@RequestBody RoadmapNodeRequest request) {
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<Boolean> deleteRoadmapById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(roadmapGeneratorService.deleteRoadmapById(id), SuccessCode.DELETE_ROADMAP_SUCCESSFUL);
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MENTOR')")
    public ApiResponse<?> saveRoadmap(@RequestBody RoadmapRequest request) {
        log.info("API Save Draft Roadmap Tree for position: {} batch: {}", request.getPositionId(),
                request.getBatchId());
        return ApiResponse.buildSuccessResponse(roadmapGeneratorService.saveRoadmap(request),
                SuccessCode.CREATE_ROADMAP_SUCCESSFUL);
    }
}