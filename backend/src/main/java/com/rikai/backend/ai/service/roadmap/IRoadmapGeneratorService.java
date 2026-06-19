package com.rikai.backend.ai.service.roadmap;

import com.rikai.backend.dto.response.roadmap.RoadmapNodeResponse;

import java.util.List;
import com.rikai.backend.dto.request.roadmap.GeneratePhasesRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapNodeRequest;
import com.rikai.backend.dto.request.roadmap.EditNodeRequest;
import com.rikai.backend.dto.request.roadmap.MoveNodeRequest;
import com.rikai.backend.dto.request.roadmap.RoadmapRequest;
import com.rikai.backend.dto.response.roadmap.NodeExpansionResponse;
import com.rikai.backend.dto.response.roadmap.RoadmapResponse;

public interface IRoadmapGeneratorService {

    List<RoadmapResponse> getAllRoadmaps();

    RoadmapResponse getRoadmapById(Long id);

    boolean deleteRoadmapById(Long id);

    List<RoadmapNodeResponse> generateMockPhases(GeneratePhasesRequest request);

    NodeExpansionResponse expandMockNode(Long nodeId, String prompt);

    RoadmapNodeResponse addNode(RoadmapNodeRequest request);

    RoadmapNodeResponse editMockNode(Long id, EditNodeRequest request);

    RoadmapNodeResponse moveMockNode(Long id, MoveNodeRequest request);

    void deleteMockNode(Long id);

    RoadmapResponse saveRoadmap(RoadmapRequest request);
}
