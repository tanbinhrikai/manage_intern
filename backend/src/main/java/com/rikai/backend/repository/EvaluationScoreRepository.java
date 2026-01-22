package com.rikai.backend.repository;

import com.rikai.backend.model.EvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Integer> {

    /**
     * Find all scores for a session
     */
    List<EvaluationScore> findBySession_Id(Integer sessionId);

    /**
     * Find score by session and criteria
     */
    Optional<EvaluationScore> findBySession_IdAndCriteria_Id(Integer sessionId, Long criteriaId);

    /**
     * Delete all scores for a session
     */
    void deleteBySession_Id(Integer sessionId);

    /**
     * Find scores for a session, joining criteria
     */
    @Query("SELECT es FROM EvaluationScore es JOIN FETCH es.criteria " +
           "WHERE es.session.id = :sessionId")
    List<EvaluationScore> findBySessionIdWithCriteria(@Param("sessionId") Integer sessionId);
}
