package com.rikai.backend.repository;

import com.rikai.backend.model.Enum.BatchStatus;
import com.rikai.backend.model.InternshipBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface InternshipBatchRepository extends JpaRepository<InternshipBatch  , Long> {
    @Query("SELECT b FROM InternshipBatch b " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR b.status = :status) ")
    Page<InternshipBatch> findBatches(
            @Param("keyword") String keyword,
            @Param("status") BatchStatus status,
            Pageable pageable
    );
    boolean existsByName(String name);
}
