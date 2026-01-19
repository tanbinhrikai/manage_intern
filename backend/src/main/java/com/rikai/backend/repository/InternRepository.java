package com.rikai.backend.repository;

import com.rikai.backend.common.InternStatus;
import com.rikai.backend.model.Intern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface InternRepository extends JpaRepository<Intern, Long> {

    @Query("""
            SELECT i FROM Intern i
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(i.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:internStatus IS NULL OR i.internStatus = :internStatus)
            AND (:positionId IS NULL OR i.position.id = :positionId)
            AND (:mentorId IS NULL OR i.mentor.id = :mentorId)
             """)
    Page<Intern> getAllInternByKeyword(Pageable pageable,
                                       @Param("keyword") String keyword,
                                       @Param("internStatus") InternStatus internStatus,
                                       @Param("positionId") Long positionId,
                                       @Param("mentorId") UUID mentorId);

    Page<Intern> findByMentor_Id(UUID mentorId, Pageable pageable);

    @Query("""
            SELECT i FROM Intern i
            WHERE i.mentor.id = :mentorId
            AND (:keyword IS NULL OR LOWER(i.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Intern> findByMentor_IdAndKeyword(@org.springframework.data.repository.query.Param("mentorId") UUID mentorId,
                                           @org.springframework.data.repository.query.Param("keyword") String keyword,
                                           Pageable pageable);

    Page<Intern> findByPosition_Id(Long positionId, Pageable pageable);

    Page<Intern> findByInternStatus(InternStatus status, Pageable pageable);

    long countByInternStatus(InternStatus status);

    @Query("""
            SELECT i FROM Intern i
            WHERE i.mentor.id = :mentorId
            AND i.internStatus != 'DROPPED'
            AND NOT EXISTS (
                SELECT 1 FROM WeeklyReport wr
                WHERE wr.intern.id = i.id
                AND wr.mentor.id = :mentorId
                AND wr.weekStartDate = :weekStartDate
            )
            ORDER BY i.fullName ASC
            """)
    Page<Intern> findInternsNotEvaluatedThisWeekByMentor(@Param("mentorId") UUID mentorId,
                                                          @Param("weekStartDate") LocalDate weekStartDate,
                                                          Pageable pageable);

    @Query("""
            SELECT i FROM Intern i
            WHERE i.internStatus != 'DROPPED'
            AND NOT EXISTS (
                SELECT 1 FROM WeeklyReport wr
                WHERE wr.intern.id = i.id
                AND wr.mentor.id = i.mentor.id
                AND wr.weekStartDate = :weekStartDate
            )
            ORDER BY i.fullName ASC
            """)
    Page<Intern> findAllInternsNotEvaluatedThisWeek(@Param("weekStartDate") LocalDate weekStartDate,
                                                      Pageable pageable);
}
