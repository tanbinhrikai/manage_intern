package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaCategoryResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.EvaluationCriteriaMapper;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.model.Enum.CriteriaCategory;
import com.rikai.backend.repository.EvaluationCriteriaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationCriteriaService implements IEvaluationCriteriaService {

    EvaluationCriteriaRepository evaluationCriteriaRepository;
    EvaluationCriteriaMapper evaluationCriteriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaCategoryResponse> getAllCriteriaHierarchy() {
        log.info("Getting all criteria in hierarchical structure");
        List<EvaluationCriteria> mainCriteria = evaluationCriteriaRepository.findAllMainCriteria();
        Map<CriteriaCategory, List<EvaluationCriteria>> criteriaByCategory = mainCriteria.stream()
                .collect(Collectors.groupingBy(EvaluationCriteria::getCategory));
        List<CriteriaCategoryResponse> result = new ArrayList<>();
        for (CriteriaCategory category : CriteriaCategory.values()) {
            List<EvaluationCriteria> categoryMainCriteria = criteriaByCategory.getOrDefault(category, new ArrayList<>());
            List<EvaluationCriteriaResponse> mainCriteriaResponses = categoryMainCriteria.stream()
                    .map(mc -> {
                        List<EvaluationCriteria> subCriteria = evaluationCriteriaRepository
                                .findSubCriteriaByParentId(mc.getId());
                        EvaluationCriteriaResponse response = evaluationCriteriaMapper.toResponse(mc);
                        response.setChildren(subCriteria.stream()
                                .map(evaluationCriteriaMapper::toResponseWithScoreDefinitions)
                                .collect(Collectors.toList()));

                        return response;
                    })
                    .collect(Collectors.toList());

            CriteriaCategoryResponse categoryResponse = CriteriaCategoryResponse.builder()
                    .category(category)
                    .displayName(category.getDisplayName())
                    .description(category.getDescription())
                    .mainCriteria(mainCriteriaResponses)
                    .build();

            result.add(categoryResponse);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getAllMainCriteria() {
        log.info("Getting all main criteria");
        List<EvaluationCriteria> mainCriteria = evaluationCriteriaRepository.findAllMainCriteria();
        return mainCriteria.stream()
                .map(evaluationCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getAllSubCriteria() {
        log.info("Getting all sub-criteria");
        List<EvaluationCriteria> subCriteria = evaluationCriteriaRepository.findAllSubCriteria();
        return subCriteria.stream()
                .map(evaluationCriteriaMapper::toResponseWithScoreDefinitions)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationCriteriaResponse getCriteriaById(Long id) {
        log.info("Getting criteria by id: {}", id);
        EvaluationCriteria criteria = evaluationCriteriaRepository.findByIdWithChildrenAndScoreDefinitions(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

        return evaluationCriteriaMapper.toResponseWithChildren(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getCriteriaByCategory(CriteriaCategory category) {
        log.info("Getting criteria by category: {}", category);
        List<EvaluationCriteria> criteria = evaluationCriteriaRepository.findByCategory(category);
        return criteria.stream()
                .map(evaluationCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CriteriaCategoryResponse> getAllCategories() {
        log.info("Getting all categories");
        return evaluationCriteriaMapper.getAllCategoryResponses();
    }

    @Override
    public List<ScoreLabelResponse> getAllScoreLabels() {
        log.info("Getting all score labels");
        return evaluationCriteriaMapper.getAllScoreLabelResponses();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getSubCriteriaByParentId(Long parentId) {
        log.info("Getting sub-criteria by parent id: {}", parentId);
        if (!evaluationCriteriaRepository.existsById(parentId)) {
            throw new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED);
        }

        List<EvaluationCriteria> subCriteria = evaluationCriteriaRepository.findSubCriteriaByParentId(parentId);
        return subCriteria.stream()
                .map(evaluationCriteriaMapper::toResponseWithScoreDefinitions)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse createEvaluationCriteria(EvaluationCriteriaCreationRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaMapper.toEvaluationCriteria(request);
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(evaluationCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse updateEvaluationCriteria(Long id, EvaluationCriteriaUpdateRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
        evaluationCriteriaMapper.updateEvaluationCriteriaFromRequest(criteria, request);
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(evaluationCriteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public void deleteEvaluationCriteria(Long id) {
        if (!evaluationCriteriaRepository.existsById(id)) {
            throw new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED);
        }
        evaluationCriteriaRepository.deleteById(id);
    }
}
