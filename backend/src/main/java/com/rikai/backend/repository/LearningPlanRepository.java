package com.rikai.backend.repository;

import com.rikai.backend.model.LearningPlan;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPlanRepository extends JpaRepository<LearningPlan, Long> {

    @EntityGraph(attributePaths = {"modules", "modules.tasks", "modules.tasks.subPlan"})
    @NonNull
    Optional<LearningPlan> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"modules", "modules.tasks"})
    List<LearningPlan> findByInternId(Long internId);

    @Query("""
            SELECT lp FROM LearningPlan lp
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(lp.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(lp.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:internId IS NULL OR lp.intern.id = :internId)
            AND NOT EXISTS (SELECT pt FROM PlanTask pt WHERE pt.subPlan = lp)
            """)
    Page<LearningPlan> findAllWithFilters(Pageable pageable,
                                          @Param("keyword") String keyword,
                                          @Param("internId") Long internId);
}