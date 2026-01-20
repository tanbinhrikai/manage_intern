package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaCategoryResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
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

    EvaluationCriteriaResponse createEvaluationCriteria(EvaluationCriteriaCreationRequest request);
    EvaluationCriteriaResponse updateEvaluationCriteria(Long id, EvaluationCriteriaUpdateRequest request);
    void deleteEvaluationCriteria(Long id);
}
