package com.rikai.backend.repository;

import com.rikai.backend.model.PlanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanTaskRepository extends JpaRepository<PlanTask , Long> {
}
