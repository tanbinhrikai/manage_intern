package com.rikai.backend.service.learningplan;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.learning_plan.LearningPlanCreationRequest;
import com.rikai.backend.dto.request.learning_plan.LearningPlanUpdateRequest;
import com.rikai.backend.dto.response.learning_plan.LearningPlanResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.LearningPlanMapper;
import com.rikai.backend.model.Enum.TaskStatus;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.LearningPlan;
import com.rikai.backend.model.PlanModule;
import com.rikai.backend.model.PlanTask;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.LearningPlanRepository;
import com.rikai.backend.repository.PlanModuleRepository;
import com.rikai.backend.repository.PlanTaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningPlanService implements ILearningPlanService {
    LearningPlanRepository learningPlanRepository;
    InternRepository internRepository;
    PlanModuleRepository planModuleRepository;
    PlanTaskRepository planTaskRepository;
    LearningPlanMapper learningPlanMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LearningPlanResponse> getAllLearningPlans(Pageable pageable, String keyword, Long internId) {
        String keywordValue = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<LearningPlan> plans = learningPlanRepository.findAllWithFilters(pageable, keywordValue, internId);
        return PageResponse.fromPage(plans.map(learningPlanMapper::toLearningPlanResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPlanResponse getLearningPlanById(Long id) {
        LearningPlan plan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_PLAN_NOT_EXISTED));
        return learningPlanMapper.toLearningPlanResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPlanResponse> getLearningPlansByInternId(Long internId) {
        List<LearningPlan> plans = learningPlanRepository.findByInternId(internId);
        return learningPlanMapper.toLearningPlanResponseList(plans);
    }

    @Override
    @Transactional
    public LearningPlanResponse createLearningPlan(LearningPlanCreationRequest request) {
        Intern intern = internRepository.findById(request.getInternId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        LearningPlan plan = LearningPlan.builder()
                .intern(intern)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        LearningPlan savedPlan = learningPlanRepository.save(plan);

        if (request.getModules() != null && !request.getModules().isEmpty()) {
            List<PlanModule> modules = new ArrayList<>();
            for (LearningPlanCreationRequest.ModuleRequest moduleReq : request.getModules()) {
                PlanModule module = PlanModule.builder()
                        .learningPlan(savedPlan)
                        .title(moduleReq.getTitle())
                        .focusTopic(moduleReq.getFocusTopic())
                        .orderIndex(moduleReq.getOrderIndex())
                        .build();
                PlanModule savedModule = planModuleRepository.save(module);

                if (moduleReq.getTasks() != null && !moduleReq.getTasks().isEmpty()) {
                    List<PlanTask> tasks = new ArrayList<>();
                    for (LearningPlanCreationRequest.TaskRequest taskReq : moduleReq.getTasks()) {
                        PlanTask task = PlanTask.builder()
                                .module(savedModule)
                                .title(taskReq.getTitle())
                                .description(taskReq.getDescription())
                                .resourceLink(taskReq.getResourceLink())
                                .estimatedMinutes(taskReq.getEstimatedMinutes())
                                .orderIndex(taskReq.getOrderIndex())
                                .status(TaskStatus.TODO)
                                .build();
                        tasks.add(task);
                    }
                    planTaskRepository.saveAll(tasks);
                    savedModule.setTasks(tasks);
                }
                modules.add(savedModule);
            }
            savedPlan.setModules(modules);
        }

        return learningPlanMapper.toLearningPlanResponse(savedPlan);
    }

    @Override
    @Transactional
    public LearningPlanResponse updateLearningPlan(Long id, LearningPlanUpdateRequest request) {
        LearningPlan plan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_PLAN_NOT_EXISTED));

        if (request.getTitle() != null) {
            plan.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        if (request.getModules() != null) {
            for (LearningPlanUpdateRequest.ModuleRequest moduleReq : request.getModules()) {
                if (moduleReq.getId() != null) {
                    PlanModule module = planModuleRepository.findById(moduleReq.getId()).orElse(null);
                    if (module != null) {
                        if (moduleReq.getTitle() != null)
                            module.setTitle(moduleReq.getTitle());
                        if (moduleReq.getFocusTopic() != null)
                            module.setFocusTopic(moduleReq.getFocusTopic());
                        if (moduleReq.getOrderIndex() != null)
                            module.setOrderIndex(moduleReq.getOrderIndex());
                        planModuleRepository.save(module);

                        if (moduleReq.getTasks() != null) {
                            for (LearningPlanUpdateRequest.TaskRequest taskReq : moduleReq.getTasks()) {
                                if (taskReq.getId() != null) {
                                    PlanTask task = planTaskRepository.findById(taskReq.getId()).orElse(null);
                                    if (task != null) {
                                        if (taskReq.getTitle() != null)
                                            task.setTitle(taskReq.getTitle());
                                        if (taskReq.getDescription() != null)
                                            task.setDescription(taskReq.getDescription());
                                        if (taskReq.getResourceLink() != null)
                                            task.setResourceLink(taskReq.getResourceLink());
                                        if (taskReq.getEstimatedMinutes() != null)
                                            task.setEstimatedMinutes(taskReq.getEstimatedMinutes());
                                        if (taskReq.getOrderIndex() != null)
                                            task.setOrderIndex(taskReq.getOrderIndex());
                                        if (taskReq.getStatus() != null) {
                                            task.setStatus(TaskStatus.valueOf(taskReq.getStatus()));
                                        }
                                        planTaskRepository.save(task);
                                    }
                                } else {
                                    PlanTask newTask = PlanTask.builder()
                                            .module(module)
                                            .title(taskReq.getTitle())
                                            .description(taskReq.getDescription())
                                            .resourceLink(taskReq.getResourceLink())
                                            .estimatedMinutes(taskReq.getEstimatedMinutes())
                                            .orderIndex(taskReq.getOrderIndex())
                                            .status(taskReq.getStatus() != null
                                                    ? TaskStatus.valueOf(taskReq.getStatus())
                                                    : TaskStatus.TODO)
                                            .build();
                                    planTaskRepository.save(newTask);
                                }
                            }
                        }
                    }
                } else {
                    PlanModule newModule = PlanModule.builder()
                            .learningPlan(plan)
                            .title(moduleReq.getTitle())
                            .focusTopic(moduleReq.getFocusTopic())
                            .orderIndex(moduleReq.getOrderIndex())
                            .build();
                    PlanModule savedModule = planModuleRepository.save(newModule);

                    if (moduleReq.getTasks() != null) {
                        for (LearningPlanUpdateRequest.TaskRequest taskReq : moduleReq.getTasks()) {
                            PlanTask newTask = PlanTask.builder()
                                    .module(savedModule)
                                    .title(taskReq.getTitle())
                                    .description(taskReq.getDescription())
                                    .resourceLink(taskReq.getResourceLink())
                                    .estimatedMinutes(taskReq.getEstimatedMinutes())
                                    .orderIndex(taskReq.getOrderIndex())
                                    .status(taskReq.getStatus() != null ? TaskStatus.valueOf(taskReq.getStatus())
                                            : TaskStatus.TODO)
                                    .build();
                            planTaskRepository.save(newTask);
                        }
                    }
                }
            }
        }

        LearningPlan updatedPlan = learningPlanRepository.save(plan);
        return learningPlanMapper.toLearningPlanResponse(updatedPlan);
    }

    @Override
    @Transactional
    public void deleteLearningPlan(Long id) {
        LearningPlan plan = learningPlanRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEARNING_PLAN_NOT_EXISTED));
        learningPlanRepository.delete(plan);
    }
}
