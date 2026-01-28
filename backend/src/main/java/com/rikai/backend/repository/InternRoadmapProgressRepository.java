package com.rikai.backend.repository;

import com.rikai.backend.model.Enum.ProgressStatus;
import com.rikai.backend.model.InternRoadmapProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternRoadmapProgressRepository extends JpaRepository<InternRoadmapProgress, Integer> {

    // Find the progress of an intern for a specific node
    Optional<InternRoadmapProgress> findByInternIdAndRoadmapNodeId(Integer internId, Integer nodeId);

    // Get the list of completed tasks of an intern
    List<InternRoadmapProgress> findByInternIdAndStatus(Integer internId, ProgressStatus status);

    // Check if this task is completed (return true/false)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM InternRoadmapProgress p " +
            "WHERE p.intern.id = :internId " +
            "AND p.roadmapNode.id = :nodeId " +
            "AND p.status = 'COMPLETED'")
    boolean isNodeCompleted(Integer internId, Integer nodeId);
}