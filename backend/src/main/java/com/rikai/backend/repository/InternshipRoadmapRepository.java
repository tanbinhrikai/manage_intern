package com.rikai.backend.repository;


import com.rikai.backend.model.InternshipRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternshipRoadmapRepository extends JpaRepository<InternshipRoadmap , Long> {
}
