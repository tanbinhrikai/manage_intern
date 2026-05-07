package com.rikai.backend.service.criteriagroup;

import com.rikai.backend.dto.request.criteria_group.CriteriaGroupCreationRequest;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;

import java.util.List;

public interface ICriteriaGroupService {
    List<CriteriaGroupResponse> getAllGroups();
    CriteriaGroupResponse getGroupById(Long id);
    CriteriaGroupResponse createGroup(CriteriaGroupCreationRequest request);
    CriteriaGroupResponse updateGroup(Long id, CriteriaGroupUpdateRequest request);
    void deleteGroup(Long id);
}
