package com.rikai.backend.repository;

import com.rikai.backend.model.RoadmapNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, Long> {
    List<RoadmapNode> findByRoadmapPositionIdAndParentIsNull(Long positionId);
    List<RoadmapNode> findByRoadmapIdAndParentIsNullOrderByOrderIndexAsc(Long roadmapId);

    Page<RoadmapNode> findByParentIdIsNull(Pageable pageable);

    long countByParentId(Long parentId);
    long countByRoadmapIdAndParentIsNull(Long roadmapId);
}