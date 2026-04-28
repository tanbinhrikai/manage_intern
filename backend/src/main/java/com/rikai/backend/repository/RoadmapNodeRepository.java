package com.rikai.backend.repository;

import com.rikai.backend.model.RoadmapNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
    List<RoadmapNode> findByPositionIdAndParentIsNull(Long positionId);

    Page<RoadmapNode> findByParentIdIsNull(Pageable pageable);
}