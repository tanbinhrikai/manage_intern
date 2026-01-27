package com.rikai.backend.repository;

import com.rikai.backend.model.LearningPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningPlanRepository  extends JpaRepository<LearningPlan , Long> {
    List<LearningPlan> findByInternId(Long internId);
}
