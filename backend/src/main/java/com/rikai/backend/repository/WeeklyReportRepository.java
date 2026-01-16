package com.rikai.backend.repository;

import com.rikai.backend.model.WeeklyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

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

        @Query("SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.intern.id = :internId ORDER BY wr.weekStartDate DESC")
        List<WeeklyReport> findByInternIdOrderByWeekStartDateDesc(@Param("internId") Long internId);

        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.mentor.id = :mentorId", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.mentor.id = :mentorId")
        Page<WeeklyReport> findByMentorId(@Param("mentorId") UUID mentorId, Pageable pageable);

        @Query(value = "SELECT wr FROM WeeklyReport wr JOIN FETCH wr.intern JOIN FETCH wr.mentor WHERE wr.mentor.id = :mentorId AND wr.intern.id = :internId", countQuery = "SELECT COUNT(wr) FROM WeeklyReport wr WHERE wr.mentor.id = :mentorId AND wr.intern.id = :internId")
        Page<WeeklyReport> findByMentorIdAndInternId(@Param("mentorId") UUID mentorId, @Param("internId") Long internId,
                        Pageable pageable);

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
}
