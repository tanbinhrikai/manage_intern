package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.CriteriaScoreDefinitionResponse;
import com.rikai.backend.model.CriteriaScoreDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CriteriaScoreDefinitionMapper {
    @Mapping(target = "criteriaId", source = "criteria.id")
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
