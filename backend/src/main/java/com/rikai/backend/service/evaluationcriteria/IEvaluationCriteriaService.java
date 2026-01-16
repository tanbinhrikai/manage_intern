package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.dto.request.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.EvaluationCriteriaResponse;

import java.util.List;

public interface IEvaluationCriteriaService {
    List<EvaluationCriteriaResponse> getAllEvaluationCriteria();

    EvaluationCriteriaResponse getEvaluationCriteriaById(Integer id);

    EvaluationCriteriaResponse createEvaluationCriteria(EvaluationCriteriaCreationRequest request);

    EvaluationCriteriaResponse updateEvaluationCriteria(Integer id, EvaluationCriteriaUpdateRequest request);

    void deleteEvaluationCriteria(Integer id);
}
