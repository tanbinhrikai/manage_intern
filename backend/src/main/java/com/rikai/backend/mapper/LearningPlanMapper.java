package com.rikai.backend.mapper;

import com.rikai.backend.dto.response.learning_plan.LearningPlanResponse;
import com.rikai.backend.dto.response.learning_plan.PlanModuleResponse;
import com.rikai.backend.dto.response.learning_plan.PlanTaskResponse;
import com.rikai.backend.model.LearningPlan;
import com.rikai.backend.model.PlanModule;
import com.rikai.backend.model.PlanTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LearningPlanMapper {
    @Mapping(target = "internId", source = "intern.id")
    @Mapping(target = "internName", source = "intern.fullName")
    @Mapping(target = "modules", source = "modules")
    LearningPlanResponse toLearningPlanResponse(LearningPlan learningPlan);

    List<LearningPlanResponse> toLearningPlanResponseList(List<LearningPlan> learningPlans);

    @Mapping(target = "tasks", source = "tasks")
    PlanModuleResponse toPlanModuleResponse(PlanModule planModule);

    List<PlanModuleResponse> toPlanModuleResponseList(List<PlanModule> planModules);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "subPlan", source = "subPlan")
    PlanTaskResponse toPlanTaskResponse(PlanTask planTask);

    List<PlanTaskResponse> toPlanTaskResponseList(List<PlanTask> planTasks);
}