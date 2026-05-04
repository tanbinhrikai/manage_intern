package com.rikai.backend.repository;

import com.rikai.backend.model.CriteriaGroup;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CriteriaGroupRepository extends JpaRepository<CriteriaGroup, Long> {
    @Override
    @NonNull
    @Query("SELECT g FROM CriteriaGroup g WHERE g.isActive = true ORDER BY g.displayOrder")
    List<CriteriaGroup> findAll();
}
