package com.rikai.backend.repository;

import com.rikai.backend.model.Department;
import com.rikai.backend.model.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position , Long> {
    @Query("""
            SELECT i FROM Position i
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
             """)
    Page<Position> getAllPositionByKeyword(Pageable pageable , @Param("keyword") String keyword);

    Optional<Position> findByTitleContainingIgnoreCase(String title);
}
