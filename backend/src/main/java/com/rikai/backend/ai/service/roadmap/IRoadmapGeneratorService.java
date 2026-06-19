package com.rikai.backend.ai.service.roadmap;

import com.rikai.backend.ai.dto.response.ChatResponseDto;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.roadmap.DraftRoadmapResponseDto;
import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;
import com.rikai.backend.model.Enum.ExpansionDepth;
import com.rikai.backend.model.RoadmapNode;
import org.springframework.data.domain.PageRequest;

import java.util.Map;
import java.util.List;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.AddNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapDto;
import com.rikai.backend.dto.request.roadmap.SaveDraftTreeRequest;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;

public interface IRoadmapGeneratorService {
    PageResponse<RoadmapNodeResponse> getAllRoadmaps(PageRequest pageRequest);

    RoadmapDto getRoadmapById(Long id);

    List<RoadmapDto> getAllRoadmaps();

    void deleteRoadmapById(Long id);

    /**
     * Generates roadmap OUTLINE only (root + phases).
     * Does NOT save to database - stores in memory as draft.
     */
    DraftRoadmapResponseDto generateOutline(String topic, String durationStr, String notes,
            Long positionId, Long batchId, String conversationId);

    /**
     * Expands a specific node in the draft roadmap.
     */
    DraftRoadmapResponseDto expandNode(String sessionId, String targetNodeTitle, ExpansionDepth depth,
            String conversationId);

    /**
     * Main entry for processing user chat messages
     */
    ChatResponseDto processUserMessage(String userMessage, Long positionId, String durationStr,
            Long batchId, String sessionId);

    /**
     * Main entry for processing user chat messages with conversation isolation
     */
    ChatResponseDto processUserMessage(String userMessage, Long positionId, String durationStr,
            Long batchId, String sessionId, String conversationId);

    Map<String, DraftRoadmapManager.DraftInfo> listAllDrafts();

    RoadmapNode confirmAndSaveDraft(String sessionId);

    void cleanupStaleDrafts();

    List<RoadmapNodeResponse> generateMockPhases(GeneratePhasesRequest request);

    NodeExpansionResponse expandMockNode(Long nodeId, String prompt);

    RoadmapNodeResponse addNode(AddNodeRequest request);

    RoadmapNodeResponse editMockNode(Long id, EditNodeRequest request);

    RoadmapNodeResponse moveMockNode(Long id, MoveNodeRequest request);

    void deleteMockNode(Long id);

    RoadmapDto saveDraftTree(SaveDraftTreeRequest request);
}
