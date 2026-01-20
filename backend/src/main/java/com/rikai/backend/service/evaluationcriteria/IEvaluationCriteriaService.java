package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.dto.response.CriteriaCategoryResponse;
import com.rikai.backend.dto.response.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.ScoreLabelResponse;
import com.rikai.backend.model.Enum.CriteriaCategory;

import java.util.List;

public interface IEvaluationCriteriaService {

    /**
     * Get all evaluation criteria (hierarchical structure)
     * Return list by category -> main criteria -> sub-criteria
     */
    List<CriteriaCategoryResponse> getAllCriteriaHierarchy();

    /**
     * Get all main criteria (without parent)
     */
    List<EvaluationCriteriaResponse> getAllMainCriteria();

    /**
     * Get all sub-criteria (can be scored)
     */
    List<EvaluationCriteriaResponse> getAllSubCriteria();

    /**
     * Get criteria by id
     */
    EvaluationCriteriaResponse getCriteriaById(Long id);

    /**
     * Get criteria by category
     */
    List<EvaluationCriteriaResponse> getCriteriaByCategory(CriteriaCategory category);

    /**
     * Get all categories
     */
    List<CriteriaCategoryResponse> getAllCategories();

    /**
     * Get all score labels
     */
    List<ScoreLabelResponse> getAllScoreLabels();

    /**
     * Get sub-criteria by parent id
     */
    List<EvaluationCriteriaResponse> getSubCriteriaByParentId(Long parentId);
}
