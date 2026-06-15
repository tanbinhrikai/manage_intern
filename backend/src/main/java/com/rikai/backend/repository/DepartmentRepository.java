package com.rikai.backend.repository;

import com.rikai.backend.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query(value = """
            SELECT i FROM Department i
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
             """)
    Page<Department> getAllDepartmentByKeyword(Pageable pageable , @Param("keyword") String keyword);
}
