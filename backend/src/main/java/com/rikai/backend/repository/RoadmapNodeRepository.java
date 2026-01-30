package com.rikai.backend.repository;

import com.rikai.backend.model.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Integer> {
    List<RoadmapNode> findByPositionIdAndParentIsNull(Integer positionId);
}