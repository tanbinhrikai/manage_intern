package com.rikai.backend.repository;

import com.rikai.backend.model.EvaluationSession;
import com.rikai.backend.model.Enum.SessionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationSessionRepository extends JpaRepository<EvaluationSession, Integer> {

    /**
     * Find evaluation session by intern ID and session type
     * Used to ensure only one session per type per intern
     */
    Optional<EvaluationSession> findByIntern_IdAndSessionType(Long internId, SessionType sessionType);

    /**
     * Find all evaluation sessions for an intern, ordered by evaluation date
     */
    @Query("SELECT es FROM EvaluationSession es JOIN FETCH es.intern JOIN FETCH es.mentor " +
            "WHERE es.intern.id = :internId ORDER BY es.evaluationDate ASC")
    List<EvaluationSession> findByInternIdOrderByEvaluationDate(@Param("internId") Long internId);

    /**
     * Find all evaluation sessions for an intern with pagination
     */
    @Query(value = "SELECT es FROM EvaluationSession es JOIN FETCH es.intern JOIN FETCH es.mentor " +
            "WHERE es.intern.id = :internId",
            countQuery = "SELECT COUNT(es) FROM EvaluationSession es WHERE es.intern.id = :internId")
    Page<EvaluationSession> findByInternId(@Param("internId") Long internId, Pageable pageable);

    /**
     * Find all evaluation sessions for a mentor
     */
    @Query(value = "SELECT es FROM EvaluationSession es JOIN FETCH es.intern JOIN FETCH es.mentor " +
            "WHERE es.mentor.id = :mentorId",
            countQuery = "SELECT COUNT(es) FROM EvaluationSession es WHERE es.mentor.id = :mentorId")
    Page<EvaluationSession> findByMentorId(@Param("mentorId") UUID mentorId, Pageable pageable);

    /**
     * Check if intern has a specific session type already
     */
    boolean existsByIntern_IdAndSessionType(Long internId, SessionType sessionType);

    /**
     * Get the latest evaluation session for an intern
     */
    @Query("SELECT es FROM EvaluationSession es WHERE es.intern.id = :internId " +
            "ORDER BY es.evaluationDate DESC LIMIT 1")
    Optional<EvaluationSession> findLatestByInternId(@Param("internId") Long internId);

    /**
     * Find evaluation sessions created after a specific date
     */
    @Query("SELECT s FROM EvaluationSession s JOIN FETCH s.intern JOIN FETCH s.mentor WHERE s.createdAt > :date")
    List<EvaluationSession> findByCreatedAtAfter(@Param("date") Instant date, Pageable pageable);

    /**
     * Find evaluation sessions updated after a specific date
     */
    @Query("SELECT s FROM EvaluationSession s JOIN FETCH s.intern JOIN FETCH s.mentor WHERE s.updatedAt > :date")
    List<EvaluationSession> findByUpdatedAtAfter(@Param("date") Instant date, Pageable pageable);
}
