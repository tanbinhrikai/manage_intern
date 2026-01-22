package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;

import java.util.List;

public interface IEvaluationCriteriaService {

    /**
     * Get all evaluation criteria grouped by Criteria Group (hierarchical structure)
     * Return list by group -> main criteria -> sub-criteria
     */
    List<CriteriaGroupResponse> getAllCriteriaHierarchy();

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
     * Get criteria by Group ID
     */
    List<EvaluationCriteriaResponse> getCriteriaByGroupId(Long groupId);

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