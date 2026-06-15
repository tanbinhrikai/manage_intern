package com.rikai.backend.repository;

import com.rikai.backend.model.Intern;
import com.rikai.backend.model.TestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestAssignmentRepository extends JpaRepository<TestAssignment,Long> {
    boolean existsByInternAndEvaluationNumber(Intern intern,int evaluationNumber);
    @Query("SELECT ta.intern.id, ta.evaluationNumber FROM TestAssignment ta WHERE ta.intern IN :interns")
    List<Object[]> findInternIdAndEvaluationNumberByInternIn(@Param("interns") List<Intern> interns);
}
