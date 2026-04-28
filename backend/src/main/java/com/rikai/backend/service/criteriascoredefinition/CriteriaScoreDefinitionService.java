package com.rikai.backend.service.criteriascoredefinition;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.criteria.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.CriteriaScoreDefinitionMapper;
import com.rikai.backend.model.CriteriaScoreDefinition;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.repository.CriteriaScoreDefinitionRepository;
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
public class CriteriaScoreDefinitionService implements ICriteriaScoreDefinitionService {
    CriteriaScoreDefinitionRepository criteriaScoreDefinitionRepository;
    EvaluationCriteriaRepository evaluationCriteriaRepository;
    CriteriaScoreDefinitionMapper criteriaScoreDefinitionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaScoreDefinitionResponse> getAllScoreDefinitions() {
        return criteriaScoreDefinitionRepository.findAll().stream()
                .map(criteriaScoreDefinitionMapper::toCriteriaScoreDefinitionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CriteriaScoreDefinitionResponse getScoreDefinitionById(Long id) {
        CriteriaScoreDefinition definition = criteriaScoreDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_SCORE_DEFINITION_NOT_EXISTED));
        return criteriaScoreDefinitionMapper.toCriteriaScoreDefinitionResponse(definition);
    }

    @Override
    @Transactional
    public CriteriaScoreDefinitionResponse createScoreDefinition(CriteriaScoreDefinitionCreationRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(request.getCriteriaId())
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

        CriteriaScoreDefinition definition = criteriaScoreDefinitionMapper.toCriteriaScoreDefinition(request);
        definition.setCriteria(criteria);

        return criteriaScoreDefinitionMapper
                .toCriteriaScoreDefinitionResponse(criteriaScoreDefinitionRepository.save(definition));
    }

    @Override
    @Transactional
    public CriteriaScoreDefinitionResponse updateScoreDefinition(Long id,
            CriteriaScoreDefinitionUpdateRequest request) {
        CriteriaScoreDefinition definition = criteriaScoreDefinitionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_SCORE_DEFINITION_NOT_EXISTED));

        if (!definition.getCriteria().getId().equals(request.getCriteriaId())) {
            EvaluationCriteria criteria = evaluationCriteriaRepository.findById(request.getCriteriaId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
            definition.setCriteria(criteria);
        }

        criteriaScoreDefinitionMapper.updateCriteriaScoreDefinitionFromRequest(definition, request);
        return criteriaScoreDefinitionMapper
                .toCriteriaScoreDefinitionResponse(criteriaScoreDefinitionRepository.save(definition));
    }

    @Override
    @Transactional
    public void deleteScoreDefinition(Long id) {
        if (!criteriaScoreDefinitionRepository.existsById(id)) {
            throw new AppException(ErrorCode.CRITERIA_SCORE_DEFINITION_NOT_EXISTED);
        }
        criteriaScoreDefinitionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaScoreDefinitionResponse> getScoreDefinitionsByCriteriaId(Long criteriaId) {
        if (!evaluationCriteriaRepository.existsById(criteriaId)) {
            throw new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED);
        }
        return criteriaScoreDefinitionRepository.findAllByCriteriaId(criteriaId).stream()
                .map(criteriaScoreDefinitionMapper::toCriteriaScoreDefinitionResponse)
                .collect(Collectors.toList());
    }
}
