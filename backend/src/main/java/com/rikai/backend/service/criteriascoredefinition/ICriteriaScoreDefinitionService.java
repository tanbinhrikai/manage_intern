package com.rikai.backend.service.criteriascoredefinition;

import com.rikai.backend.dto.request.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.CriteriaScoreDefinitionResponse;

import java.util.List;

public interface ICriteriaScoreDefinitionService {
    List<CriteriaScoreDefinitionResponse> getAllScoreDefinitions();

    CriteriaScoreDefinitionResponse getScoreDefinitionById(Long id);

    CriteriaScoreDefinitionResponse createScoreDefinition(CriteriaScoreDefinitionCreationRequest request);

    CriteriaScoreDefinitionResponse updateScoreDefinition(Long id, CriteriaScoreDefinitionUpdateRequest request);

    void deleteScoreDefinition(Long id);

    List<CriteriaScoreDefinitionResponse> getScoreDefinitionsByCriteriaId(Long criteriaId);
}
