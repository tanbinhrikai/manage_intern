package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.EvaluationCriteriaResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.EvaluationCriteriaMapper;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.repository.EvaluationCriteriaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationCriteriaService implements IEvaluationCriteriaService {
    EvaluationCriteriaRepository evaluationCriteriaRepository;
    EvaluationCriteriaMapper evaluationCriteriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getAllEvaluationCriteria() {
        return evaluationCriteriaRepository.findAll().stream()
                .map(evaluationCriteriaMapper::toEvaluationCriteriaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationCriteriaResponse getEvaluationCriteriaById(Integer id) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(criteria);
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse createEvaluationCriteria(EvaluationCriteriaCreationRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaMapper.toEvaluationCriteria(request);
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(evaluationCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse updateEvaluationCriteria(Integer id, EvaluationCriteriaUpdateRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
        evaluationCriteriaMapper.updateEvaluationCriteriaFromRequest(criteria, request);
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(evaluationCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public void deleteEvaluationCriteria(Integer id) {
        if (!evaluationCriteriaRepository.existsById(id)) {
            throw new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED);
        }
        evaluationCriteriaRepository.deleteById(id);
    }
}
