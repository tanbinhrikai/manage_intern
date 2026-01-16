package com.rikai.backend.repository;

import com.rikai.backend.model.CriteriaScoreDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriteriaScoreDefinitionRepository extends JpaRepository<CriteriaScoreDefinition, Integer> {
    List<CriteriaScoreDefinition> findAllByCriteriaId(Integer criteriaId);
}
