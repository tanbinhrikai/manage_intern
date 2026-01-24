package com.rikai.backend.repository;

import com.rikai.backend.model.InternshipBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InternshipBatchRepository extends JpaRepository<InternshipBatch, Long> {
    @Query("""
            SELECT ib FROM InternshipBatch ib
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(ib.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
             """)
    Page<InternshipBatch> getAllInternshipBatchByKeyword(String keyword, Pageable pageable);
}
