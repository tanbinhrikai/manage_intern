package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.EvaluationCriteriaResponse;
import com.rikai.backend.model.EvaluationCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { CriteriaScoreDefinitionMapper.class })
public interface EvaluationCriteriaMapper {
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions")
    EvaluationCriteriaResponse toEvaluationCriteriaResponse(EvaluationCriteria criteria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    EvaluationCriteria toEvaluationCriteria(EvaluationCriteriaCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    void updateEvaluationCriteriaFromRequest(@MappingTarget EvaluationCriteria criteria,
            EvaluationCriteriaUpdateRequest request);
}
