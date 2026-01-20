package com.rikai.backend.repository;

import com.rikai.backend.model.CriteriaScoreDefinition;
import com.rikai.backend.model.Enum.ScoreLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CriteriaScoreDefinitionRepository extends JpaRepository<CriteriaScoreDefinition, Long> {

    /**
     * Get all score definitions by criteria id
     */
    List<CriteriaScoreDefinition> findAllByCriteriaId(Long criteriaId);

    /**
     *  Get score definition by criteria id and score label
     */
    Optional<CriteriaScoreDefinition> findByCriteriaIdAndScoreLabel(Long criteriaId, ScoreLabel scoreLabel);

    /**
     * Get all score definitions by criteria id ordered by score descending
     */
    @Query("SELECT csd FROM CriteriaScoreDefinition csd " +
           "WHERE csd.criteria.id = :criteriaId " +
           "ORDER BY CASE csd.scoreLabel " +
           "  WHEN 'EXCELLENT' THEN 1 " +
           "  WHEN 'GOOD' THEN 2 " +
           "  WHEN 'AVERAGE' THEN 3 " +
           "  WHEN 'WEAK' THEN 4 END")
    List<CriteriaScoreDefinition> findAllByCriteriaIdOrderByScoreDesc(@Param("criteriaId") Long criteriaId);
}
