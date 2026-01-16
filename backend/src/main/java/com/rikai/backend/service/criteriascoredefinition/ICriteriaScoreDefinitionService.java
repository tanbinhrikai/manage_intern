package com.rikai.backend.service.criteriascoredefinition;

import com.rikai.backend.dto.request.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.CriteriaScoreDefinitionResponse;

import java.util.List;

public interface ICriteriaScoreDefinitionService {
    List<CriteriaScoreDefinitionResponse> getAllScoreDefinitions();

    CriteriaScoreDefinitionResponse getScoreDefinitionById(Integer id);

    CriteriaScoreDefinitionResponse createScoreDefinition(CriteriaScoreDefinitionCreationRequest request);

    CriteriaScoreDefinitionResponse updateScoreDefinition(Integer id, CriteriaScoreDefinitionUpdateRequest request);

    void deleteScoreDefinition(Integer id);

    List<CriteriaScoreDefinitionResponse> getScoreDefinitionsByCriteriaId(Integer criteriaId);
}
