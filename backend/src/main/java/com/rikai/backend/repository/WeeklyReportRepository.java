package com.rikai.backend.repository;

import com.rikai.backend.model.WeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Integer> {

    /**
     * Find a weekly report by intern_id and week_start_date
     * 
     * @param internId
     * @param weekStartDate
     * @return
     */
    Optional<WeeklyReport> findByInternIdAndWeekStartDate(Long internId, LocalDate weekStartDate);

    /**
     * Find all reports by intern_id with pagination
     * 
     * @param internId
     * @param pageable
     * @return
     */
    Page<WeeklyReport> findByInternId(Long internId, Pageable pageable);

    /**
     * Find all reports by intern_id ordered by week_start_date descending
     * 
     * @param internId
     * @return
     */
    List<WeeklyReport> findByInternIdOrderByWeekStartDateDesc(Long internId);

    /**
     * Find reports by mentor_id with pagination
     * 
     * @param mentorId
     * @param pageable
     * @return
     */
    Page<WeeklyReport> findByMentorId(UUID mentorId, Pageable pageable);

    /**
     * Find reports by mentor_id and intern_id with pagination
     * 
     * @param mentorId
     * @param internId
     * @param pageable
     * @return
     */
    Page<WeeklyReport> findByMentorIdAndInternId(UUID mentorId, Long internId, Pageable pageable);

    /**
     * Find reports by intern_id within a date range
     * 
     * @param internId
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("SELECT wr FROM WeeklyReport wr WHERE wr.intern.id = :internId " +
            "AND wr.weekStartDate >= :startDate AND wr.weekStartDate <= :endDate " +
            "ORDER BY wr.weekStartDate DESC")
    List<WeeklyReport> findByInternIdAndDateRange(
            @Param("internId") Long internId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count reports by intern_id
     * 
     * @param internId
     * @return
     */
    long countByInternId(Long internId);
}
