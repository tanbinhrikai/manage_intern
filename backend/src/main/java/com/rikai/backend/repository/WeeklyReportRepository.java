package com.rikai.backend.repository;

import com.rikai.backend.model.WeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Integer> {

        @Query("SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.intern.id = :internId AND wr.weekStartDate = :weekStartDate")
        Optional<WeeklyReport> findByInternIdAndWeekStartDate(@Param("internId") Long internId,
                        @Param("weekStartDate") LocalDate weekStartDate);

        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.intern.id = :internId", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.intern.id = :internId")
        Page<WeeklyReport> findByInternId(@Param("internId") Long internId, Pageable pageable);

        /**
         * Find weekly reports by intern ID ordered by week start date in descending
         * order.
         *
         * @param internId the ID of the intern
         * @param pageable the pagination information
         * @return a page of weekly reports for the specified intern ordered by week
         *         start date descending
         */
        @Query("SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.intern.id = :internId ORDER BY wr.weekStartDate DESC")
        Page<WeeklyReport> findByInternIdOrderByWeekStartDateDesc(@Param("internId") Long internId, Pageable pageable);

        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.mentor.id = :mentorId", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.mentor.id = :mentorId")
        Page<WeeklyReport> findByMentorId(@Param("mentorId") UUID mentorId, Pageable pageable);

        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.mentor.id = :mentorId AND wr.intern.id = :internId", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.mentor.id = :mentorId AND wr.intern.id = :internId")
        Page<WeeklyReport> findByMentorIdAndInternId(@Param("mentorId") UUID mentorId, @Param("internId") Long internId,
                        Pageable pageable);

        /**
         * Find weekly reports by intern ID within a specified date range ordered by
         * week start date in descending order.
         *
         * @param internId  the ID of the intern
         * @param startDate the start date of the range
         * @param endDate   the end date of the range
         * @return a list of weekly reports for the specified intern within the date
         *         range ordered by week start date descending
         */
        @Query("SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.intern.id = :internId "
                        +
                        "AND wr.weekStartDate >= :startDate AND wr.weekStartDate <= :endDate " +
                        "ORDER BY wr.weekStartDate DESC")
        List<WeeklyReport> findByInternIdAndDateRange(
                        @Param("internId") Long internId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        long countByInternId(Long internId);

        @Override
        @NonNull
        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr")
        Page<WeeklyReport> findAll(@NonNull Pageable pageable);

        /**
         * Find a weekly report by its ID, including its details and associated
         * criteria.
         *
         * @param id the ID of the weekly report
         * @return an Optional containing the weekly report with its details and
         *         criteria if found, otherwise empty
         */
        @Query("SELECT wr FROM WeeklyReport wr LEFT JOIN FETCH wr.details d LEFT JOIN FETCH d.criteria WHERE wr.id = :id")
        Optional<WeeklyReport> findByIdWithDetails(@Param("id") Integer id);

        // Select new weekly reports (Filtered by createdAt and updatedAt)
        @Query("SELECT r FROM WeeklyReport r JOIN FETCH r.intern JOIN FETCH r.mentor WHERE r.createdAt > :date")
        List<WeeklyReport> findByCreatedAtAfter(@Param("date") Instant date, Pageable pageable);

        // Select updated weekly reports (Filtered by updatedAt)
        @Query("SELECT r FROM WeeklyReport r JOIN FETCH r.intern JOIN FETCH r.mentor WHERE r.updatedAt > :date")
        List<WeeklyReport> findByUpdatedAtAfter(@Param("date") Instant date, Pageable pageable);

        /**
         * Find weekly reports by mentor ID with optional filtering by intern ID and
         * date range.
         * Used for the my-reports endpoint.
         *
         * @param mentorId  the ID of the mentor
         * @param internId  optional intern ID to filter by
         * @param startDate optional start date for filtering
         * @param endDate   optional end date for filtering
         * @param pageable  pagination information
         * @return a page of weekly reports matching the filters
         */
        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor " +
                        "WHERE wr.mentor.id = :mentorId " +
                        "AND (:internId IS NULL OR wr.intern.id = :internId) " +
                        "AND (:startDate IS NULL OR wr.weekStartDate >= :startDate) " +
                        "AND (:endDate IS NULL OR wr.weekStartDate <= :endDate)", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr "
                                        +
                                        "WHERE wr.mentor.id = :mentorId " +
                                        "AND (:internId IS NULL OR wr.intern.id = :internId) " +
                                        "AND (:startDate IS NULL OR wr.weekStartDate >= :startDate) " +
                                        "AND (:endDate IS NULL OR wr.weekStartDate <= :endDate)")
        Page<WeeklyReport> findByMentorIdWithFilters(
                        @Param("mentorId") UUID mentorId,
                        @Param("internId") Long internId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);
}
