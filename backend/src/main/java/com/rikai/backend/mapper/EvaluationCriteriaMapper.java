package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaCategoryResponse;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
import com.rikai.backend.model.CriteriaScoreDefinition;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.model.Enum.CriteriaCategory;
import com.rikai.backend.model.Enum.ScoreLabel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EvaluationCriteriaMapper {

    @Mapping(target = "categoryDisplayName", expression = "java(criteria.getCategory().getDisplayName())")
    @Mapping(target = "categoryDescription", expression = "java(criteria.getCategory().getDescription())")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    EvaluationCriteriaResponse toEvaluationCriteriaResponse(EvaluationCriteria criteria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    EvaluationCriteria toEvaluationCriteria(EvaluationCriteriaCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEvaluationCriteriaFromRequest(@MappingTarget EvaluationCriteria criteria,
                                             EvaluationCriteriaUpdateRequest request);

    /**
     * Map EvaluationCriteria entity to response (not include children)
     */
    @Mapping(target = "categoryDisplayName", expression = "java(criteria.getCategory().getDisplayName())")
    @Mapping(target = "categoryDescription", expression = "java(criteria.getCategory().getDescription())")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    EvaluationCriteriaResponse toResponse(EvaluationCriteria criteria);

    /**
     * Map EvaluationCriteria entity to response (include children)
     */
    @Mapping(target = "categoryDisplayName", expression = "java(criteria.getCategory().getDisplayName())")
    @Mapping(target = "categoryDescription", expression = "java(criteria.getCategory().getDescription())")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", source = "children", qualifiedByName = "toResponseListWithoutChildren")
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
    EvaluationCriteriaResponse toResponseWithChildren(EvaluationCriteria criteria);

    /**
     * Map list without children (to avoid infinite recursion)
     */
    @Named("toResponseListWithoutChildren")
    default List<EvaluationCriteriaResponse> toResponseListWithoutChildren(List<EvaluationCriteria> criteriaList) {
        if (criteriaList == null) return null;
        return criteriaList.stream()
                .filter(c -> c.getIsActive())
                .map(this::toResponseWithScoreDefinitions)
                .collect(Collectors.toList());
    }

    /**
     * Map with score definitions but without children
     */
    @Mapping(target = "categoryDisplayName", expression = "java(criteria.getCategory().getDisplayName())")
    @Mapping(target = "categoryDescription", expression = "java(criteria.getCategory().getDescription())")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "scoreDefinitions", source = "scoreDefinitions", qualifiedByName = "toScoreDefinitionResponseList")
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }

    /**
     * Map CriteriaCategory to CriteriaCategoryResponse (not include criteria list)
     */
    default CriteriaCategoryResponse toCategoryResponse(CriteriaCategory category) {
        return CriteriaCategoryResponse.builder()
                .category(category)
                .displayName(category.getDisplayName())
                .description(category.getDescription())
                .mainCriteria(null)
                .build();
    }

    /**
     * Get all CriteriaCategory as response
     */
    default List<CriteriaCategoryResponse> getAllCategoryResponses() {
        return Arrays.stream(CriteriaCategory.values())
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }
}
