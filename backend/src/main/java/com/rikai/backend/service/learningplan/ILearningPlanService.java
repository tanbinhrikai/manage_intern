package com.rikai.backend.service.learningplan;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.learning_plan.LearningPlanCreationRequest;
import com.rikai.backend.dto.request.learning_plan.LearningPlanUpdateRequest;
import com.rikai.backend.dto.response.learning_plan.LearningPlanResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILearningPlanService {

    PageResponse<LearningPlanResponse> getAllLearningPlans(Pageable pageable, String keyword, Long internId);

    LearningPlanResponse getLearningPlanById(Long id);

    List<LearningPlanResponse> getLearningPlansByInternId(Long internId);

    LearningPlanResponse createLearningPlan(LearningPlanCreationRequest request);

    LearningPlanResponse updateLearningPlan(Long id, LearningPlanUpdateRequest request);

    void deleteLearningPlan(Long id);
}
