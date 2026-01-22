package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.criteria_group.CriteriaGroupCreationRequest;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;
import com.rikai.backend.model.CriteriaGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CriteriaGroupMapper {
    @Mapping(target = "mainCriteria" , ignore = true)
    CriteriaGroupResponse toResponse(CriteriaGroup group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criteriaList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CriteriaGroup toEntity(CriteriaGroupCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criteriaList", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget CriteriaGroup group, CriteriaGroupUpdateRequest request);
}