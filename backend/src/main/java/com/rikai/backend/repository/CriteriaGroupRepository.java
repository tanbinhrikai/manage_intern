package com.rikai.backend.repository;

import com.rikai.backend.model.CriteriaGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaGroupRepository extends JpaRepository<CriteriaGroup, Long> {
}
