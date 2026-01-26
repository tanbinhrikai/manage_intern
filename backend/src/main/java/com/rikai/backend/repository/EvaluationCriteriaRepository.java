package com.rikai.backend.repository;

import com.rikai.backend.model.EvaluationCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationCriteriaRepository extends JpaRepository<EvaluationCriteria, Long> {

    /**
     * Get all criteria with score definitions
     */
    @Override
    @NonNull
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.isActive = true " +
            "ORDER BY ec.group.displayOrder, ec.displayOrder")
    List<EvaluationCriteria> findAll();

    /**
     * Get all main criteria (without parent) with score definitions
     */
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.parent IS NULL AND ec.isActive = true " +
            "ORDER BY ec.group.displayOrder, ec.displayOrder")
    List<EvaluationCriteria> findAllMainCriteria();

    /**
     * Get all main criteria by category with score definitions
     */
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.parent IS NULL AND ec.group.id = :groupId AND ec.isActive = true " +
            "ORDER BY ec.displayOrder")
    List<EvaluationCriteria> findMainCriteriaByGroupId(@Param("groupId") Long groupId);

    /**
     * Get all sub-criteria by parent id
     */
//    @Query("SELECT ec FROM EvaluationCriteria ec " +
//           "LEFT JOIN FETCH ec.scoreDefinitions " +
//           "WHERE ec.parent.id = :parentId AND ec.isActive = true " +
//           "ORDER BY ec.displayOrder")
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.parent.id = :parentId AND ec.isActive = true " +
            "ORDER BY ec.displayOrder")
    List<EvaluationCriteria> findSubCriteriaByParentId(@Param("parentId") Long parentId);

    /**
     * Get all sub-criteria (with parent) with score definitions
     */
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "JOIN FETCH ec.parent p " +
            "WHERE ec.parent IS NOT NULL AND ec.isActive = true " +
            "ORDER BY ec.group.displayOrder, ec.parent.displayOrder, ec.displayOrder")
    List<EvaluationCriteria> findAllSubCriteria();

    /**
     * Get all criteria by category with score definitions
     */
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.group.id = :groupId AND ec.isActive = true " +
            "ORDER BY ec.displayOrder")
    List<EvaluationCriteria> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Get criteria by id with children and score definitions
     */
    @Query("SELECT ec FROM EvaluationCriteria ec " +
            "LEFT JOIN FETCH ec.children c " +
            "LEFT JOIN FETCH c.scoreDefinitions " +
            "LEFT JOIN FETCH ec.scoreDefinitions " +
            "WHERE ec.id = :id")
    Optional<EvaluationCriteria> findByIdWithChildrenAndScoreDefinitions(@Param("id") Long id);

    /**
     * Count sub-criteria by parent id
     */
    @Query("SELECT COUNT(ec) FROM EvaluationCriteria ec WHERE ec.parent.id = :parentId AND ec.isActive = true")
    Long countSubCriteriaByParentId(@Param("parentId") Long parentId);
}