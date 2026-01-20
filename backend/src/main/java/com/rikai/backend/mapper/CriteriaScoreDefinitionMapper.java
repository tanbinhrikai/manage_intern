package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import com.rikai.backend.model.CriteriaScoreDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CriteriaScoreDefinitionMapper {

    @Mapping(target = "criteriaId", source = "criteria.id")
//    @Mapping(target = "scoreLabelDisplayName", expression = "java(definition.getScoreLabel().getDisplayName())")
//    @Mapping(target = "scoreLabelDescription", expression = "java(definition.getScoreLabel().getDescription())")
    @Mapping(target = "minScore", expression = "java(definition.getScoreLabel().getMinScore())")
    @Mapping(target = "maxScore", expression = "java(definition.getScoreLabel().getMaxScore())")
    CriteriaScoreDefinitionResponse toCriteriaScoreDefinitionResponse(CriteriaScoreDefinition definition);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CriteriaScoreDefinition toCriteriaScoreDefinition(CriteriaScoreDefinitionCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criteria", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateCriteriaScoreDefinitionFromRequest(@MappingTarget CriteriaScoreDefinition definition,
            CriteriaScoreDefinitionUpdateRequest request);
}
