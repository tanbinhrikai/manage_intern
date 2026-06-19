package com.rikai.backend.repository;

import com.rikai.backend.model.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.rikai.backend.model.Enum.RoadmapStatus;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByStatus(RoadmapStatus status);
}
