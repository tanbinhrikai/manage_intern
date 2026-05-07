package com.rikai.backend.service.criteriascoredefinition;

import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;

import java.util.List;

public interface ICriteriaScoreDefinitionService {
    List<CriteriaScoreDefinitionResponse> getAllScoreDefinitions();

    CriteriaScoreDefinitionResponse getScoreDefinitionById(Long id);

    CriteriaScoreDefinitionResponse createScoreDefinition(CriteriaScoreDefinitionCreationRequest request);

    CriteriaScoreDefinitionResponse updateScoreDefinition(Long id, CriteriaScoreDefinitionUpdateRequest request);

    void deleteScoreDefinition(Long id);

    List<CriteriaScoreDefinitionResponse> getScoreDefinitionsByCriteriaId(Long criteriaId);
}
