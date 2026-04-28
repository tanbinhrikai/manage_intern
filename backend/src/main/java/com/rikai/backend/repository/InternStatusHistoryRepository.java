package com.rikai.backend.repository;

import com.rikai.backend.model.InternStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface InternStatusHistoryRepository extends JpaRepository<InternStatusHistory, Integer> {
    /* Retrieves all intern status history records with pagination,
       ordered by the change timestamp in descending order.
    */
    @Query(value = "SELECT ish FROM InternStatusHistory ish JOIN FETCH ish.intern JOIN FETCH ish.changedBy " +
                   "ORDER BY ish.changedAt DESC",
           countQuery = "SELECT COUNT(ish) FROM InternStatusHistory ish")
    Page<InternStatusHistory> findAllOrderByChangedAtDesc(Pageable pageable);

    /* Retrieves intern status history records that were changed after a specified timestamp,
       with pagination, ordered by the change timestamp in descending order.
    */
    @Query(value = "SELECT ish FROM InternStatusHistory ish JOIN FETCH ish.intern JOIN FETCH ish.changedBy " +
                   "WHERE ish.changedAt >= :since ORDER BY ish.changedAt DESC",
           countQuery = "SELECT COUNT(ish) FROM InternStatusHistory ish WHERE ish.changedAt >= :since")
    Page<InternStatusHistory> findRecentStatusChanges(@Param("since") Instant since, Pageable pageable);
}
