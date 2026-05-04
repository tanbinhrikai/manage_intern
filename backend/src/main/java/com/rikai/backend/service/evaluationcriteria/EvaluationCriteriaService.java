package com.rikai.backend.service.evaluationcriteria;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.EvaluationCriteriaMapper;
import com.rikai.backend.model.CriteriaGroup;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.repository.CriteriaGroupRepository;
import com.rikai.backend.repository.EvaluationCriteriaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationCriteriaService implements IEvaluationCriteriaService {

    EvaluationCriteriaRepository evaluationCriteriaRepository;
    EvaluationCriteriaMapper evaluationCriteriaMapper;
    CriteriaGroupRepository criteriaGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaGroupResponse> getAllCriteriaHierarchy() {
        List<CriteriaGroup> groups = criteriaGroupRepository.findAll();
        List<CriteriaGroupResponse> result = new ArrayList<>();

        for (CriteriaGroup group : groups) {
            var mainCriteriaList = evaluationCriteriaRepository.findMainCriteriaByGroupId(group.getId());

            var mainCriteriaResponses = mainCriteriaList.stream()
                    .map(mc -> {
                        List<EvaluationCriteria> subCriteria = evaluationCriteriaRepository.findSubCriteriaByParentId(mc.getId());

                        var response = evaluationCriteriaMapper.toResponse(mc);
                        response.setChildren(subCriteria.stream()
                                .map(evaluationCriteriaMapper::toResponseWithScoreDefinitions)
                                .toList());
                        return response;
                    })
                    .toList();

            result.add(CriteriaGroupResponse.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .displayOrder(group.getDisplayOrder())
                    .mainCriteria(mainCriteriaResponses)
                    .build());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getAllMainCriteria() {
        log.info("Getting all main criteria");
        List<EvaluationCriteria> mainCriteria = evaluationCriteriaRepository.findAllMainCriteria();
        mainCriteria.forEach(criteria -> {
            if (criteria.getScoreDefinitions() != null) {
                criteria.getScoreDefinitions().size();
            }
        });
        return mainCriteria.stream()
                .map(evaluationCriteriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationCriteriaResponse> getAllSubCriteria() {
        log.info("Getting all sub-criteria");
        List<EvaluationCriteria> subCriteria = evaluationCriteriaRepository.findAllSubCriteria();
        subCriteria.forEach(criteria -> {
            if (criteria.getScoreDefinitions() != null) {
                criteria.getScoreDefinitions().size();
            }
        });
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
    public List<EvaluationCriteriaResponse> getCriteriaByGroupId(Long groupId) {
        if (!criteriaGroupRepository.existsById(groupId)) {
            throw new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED);
        }
        var criteria = evaluationCriteriaRepository.findByGroupId(groupId);
        return criteria.stream().map(evaluationCriteriaMapper::toEvaluationCriteriaResponse).toList();
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
        subCriteria.forEach(criteria -> {
            if (criteria.getScoreDefinitions() != null) {
                criteria.getScoreDefinitions().size();
            }
        });
        return subCriteria.stream()
                .map(evaluationCriteriaMapper::toResponseWithScoreDefinitions)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse createEvaluationCriteria(EvaluationCriteriaCreationRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaMapper.toEvaluationCriteria(request);
        applyDefaults(criteria);

        CriteriaGroup group = criteriaGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED));
        criteria.setGroup(group);

        if (request.getParentId() != null) {
            EvaluationCriteria parent = evaluationCriteriaRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

            if (parent.getParent() != null) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_PARENT);
            }
            if (!parent.getGroup().getId().equals(group.getId())) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_GROUP);
            }
            criteria.setParent(parent);
        }

        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(
                evaluationCriteriaRepository.save(criteria)
        );
    }

    @Override
    @Transactional
    public EvaluationCriteriaResponse updateEvaluationCriteria(Long id, EvaluationCriteriaUpdateRequest request) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

        evaluationCriteriaMapper.updateEvaluationCriteriaFromRequest(criteria, request);

        if (request.getGroupId() != null) {
            CriteriaGroup group = criteriaGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED));
            criteria.setGroup(group);
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_PARENT);
            }

            EvaluationCriteria parent = evaluationCriteriaRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

            if (parent.getParent() != null) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_PARENT);
            }
            if (!parent.getGroup().getId().equals(criteria.getGroup().getId())) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_GROUP);
            }
            criteria.setParent(parent);
        } else if (criteria.getParent() != null && request.getGroupId() != null) {
            if (!criteria.getParent().getGroup().getId().equals(criteria.getGroup().getId())) {
                throw new AppException(ErrorCode.INVALID_CRITERIA_GROUP);
            }
        }

        applyDefaults(criteria);
        return evaluationCriteriaMapper.toEvaluationCriteriaResponse(
                evaluationCriteriaRepository.save(criteria)
        );
    }

    @Override
    @Transactional
    public void deleteEvaluationCriteria(Long id) {
        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

        softDeleteCriteriaTree(criteria);
    }

    @Transactional
    private void softDeleteCriteriaTree(EvaluationCriteria criteria) {
        if (criteria == null || Boolean.FALSE.equals(criteria.getIsActive())) {
            return;
        }

        List<EvaluationCriteria> children = evaluationCriteriaRepository.findSubCriteriaByParentId(criteria.getId());
        for (EvaluationCriteria child : children) {
            softDeleteCriteriaTree(child);
        }

        criteria.setIsActive(false);
        criteria.setDeletedAt(Instant.now());
        evaluationCriteriaRepository.save(criteria);
    }

    private void applyDefaults(EvaluationCriteria criteria) {
        if (criteria.getWeight() == null) {
            criteria.setWeight(BigDecimal.ONE);
        }
        if (criteria.getDisplayOrder() == null) {
            criteria.setDisplayOrder(1);
        }
        if (criteria.getIsActive() == null) {
            criteria.setIsActive(true);
        }
    }
}