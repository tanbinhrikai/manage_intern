package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
import com.rikai.backend.model.CriteriaScoreDefinition;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.model.Enum.ScoreLabel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring" , nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
public interface EvaluationCriteriaMapper {

    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    EvaluationCriteriaResponse toEvaluationCriteriaResponse(EvaluationCriteria criteria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    @Mapping(target = "group", ignore = true)
    EvaluationCriteria toEvaluationCriteria(EvaluationCriteriaCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    @Mapping(target = "group", ignore = true)
    void updateEvaluationCriteriaFromRequest(@MappingTarget EvaluationCriteria criteria,
                                             EvaluationCriteriaUpdateRequest request);

    /**
     * Map EvaluationCriteria entity to response (not include children)
     */
    @Mapping(target = "groupId", source = "group.id")
    @Mapping(target = "groupName", source = "group.name")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    EvaluationCriteriaResponse toResponse(EvaluationCriteria criteria);

    /**
     * Map EvaluationCriteria entity to response (include children)
     */
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", source = "children", qualifiedByName = "toResponseListWithoutChildren")
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    @Mapping(target = "groupId" , source = "group.id")
    @Mapping(target = "groupName" , source = "group.name")
    EvaluationCriteriaResponse toResponseWithChildren(EvaluationCriteria criteria);

    /**
     * Map list without children (to avoid infinite recursion)
     */
    @Named("toResponseListWithoutChildren")
    default List<EvaluationCriteriaResponse> toResponseListWithoutChildren(List<EvaluationCriteria> criteriaList) {
        if (criteriaList == null) return null;
        return criteriaList.stream()
                .filter(EvaluationCriteria::getIsActive)
                .map(this::toResponseWithScoreDefinitions)
                .toList();
    }

    /**
     * Map with score definitions but without children
     */
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    @Mapping(target = "groupId" , source = "group.id")
    @Mapping(target = "groupName" , source = "group.name")
    EvaluationCriteriaResponse toResponseWithScoreDefinitions(EvaluationCriteria criteria);

    /**
     * Map CriteriaScoreDefinition entity to response
     */
    @Mapping(target = "criteriaId", source = "criteria.id")
    @Mapping(target = "minScore", expression = "java(definition.getScoreLabel().getMinScore())")
    @Mapping(target = "maxScore", expression = "java(definition.getScoreLabel().getMaxScore())")
    CriteriaScoreDefinitionResponse toScoreDefinitionResponse(CriteriaScoreDefinition definition);

    @Named("toScoreDefinitionResponseList")
    default List<CriteriaScoreDefinitionResponse> toScoreDefinitionResponseList(List<CriteriaScoreDefinition> definitions) {
        if (definitions == null) return null;
        return definitions.stream()
                .map(this::toScoreDefinitionResponse)
                .toList();
    }

    /**
     * Map ScoreLabel enum to response
     */
    default ScoreLabelResponse toScoreLabelResponse(ScoreLabel scoreLabel) {
        return ScoreLabelResponse.builder()
                .value(scoreLabel)
                .minScore(scoreLabel.getMinScore())
                .maxScore(scoreLabel.getMaxScore())
                .build();
    }

    /**
     * Get all ScoreLabel as response
     */
    default List<ScoreLabelResponse> getAllScoreLabelResponses() {
        return Arrays.stream(ScoreLabel.values())
                .map(this::toScoreLabelResponse)
                .toList();
    }
}
