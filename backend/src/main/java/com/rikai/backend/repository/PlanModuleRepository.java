package com.rikai.backend.repository;

import com.rikai.backend.model.PlanModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanModuleRepository extends JpaRepository<PlanModule , Long> {
}
