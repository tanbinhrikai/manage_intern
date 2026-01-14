package com.rikai.backend.repository;

import com.rikai.backend.common.InternStatus;
import com.rikai.backend.model.Intern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InternRepository extends JpaRepository<Intern, Long> {
    Page<Intern> findByMentor_Id(UUID mentorId, Pageable pageable);

    Page<Intern> findByPosition_Id(Long positionId, Pageable pageable);

    Page<Intern> findByInternStatus(InternStatus status, Pageable pageable);
}
