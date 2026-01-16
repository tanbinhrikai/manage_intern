package com.rikai.backend.repository;

import com.rikai.backend.model.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

import org.springframework.lang.NonNull;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Integer> {
    @Override
    @NonNull
    @Query("SELECT ec FROM EvaluationCriteria ec LEFT JOIN FETCH ec.scoreDefinitions")
    List<EvaluationCriteria> findAll();
}
