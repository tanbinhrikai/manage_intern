package com.rikai.backend.service.criteriagroup;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupCreationRequest;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.CriteriaGroupMapper;
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

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CriteriaGroupService implements ICriteriaGroupService {

    CriteriaGroupRepository criteriaGroupRepository;
    CriteriaGroupMapper criteriaGroupMapper;
    EvaluationCriteriaRepository evaluationCriteriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaGroupResponse> getAllGroups() {
        List<CriteriaGroup> criteriaGroups = criteriaGroupRepository.findAll();
        return criteriaGroups.stream().map(criteriaGroupMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CriteriaGroupResponse getGroupById(Long id) {
        CriteriaGroup group = criteriaGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED));
        return criteriaGroupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public CriteriaGroupResponse createGroup(CriteriaGroupCreationRequest request) {
        CriteriaGroup group = criteriaGroupRepository.save(criteriaGroupMapper.toEntity(request));
        return criteriaGroupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public CriteriaGroupResponse updateGroup(Long id, CriteriaGroupUpdateRequest request) {
        CriteriaGroup group = criteriaGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED));
        criteriaGroupMapper.updateEntity(group, request);
        return criteriaGroupMapper.toResponse(criteriaGroupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        CriteriaGroup group = criteriaGroupRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_GROUP_NOT_EXISTED));

        List<EvaluationCriteria> mainCriteriaList = evaluationCriteriaRepository.findMainCriteriaByGroupId(id);
        for (EvaluationCriteria criteria : mainCriteriaList) {
            softDeleteCriteriaTree(criteria);
        }

        group.setIsActive(false);
        group.setDeletedAt(Instant.now());
        criteriaGroupRepository.save(group);
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
}