package com.rikai.backend.repository;

import com.rikai.backend.model.Intern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InternRepository extends JpaRepository<Intern, Integer> {

    /**
     * Find interns by mentor_id with pagination
     * This allows fetching interns associated with a specific mentor
     */
    Page<Intern> findByMentorId(UUID mentorId, Pageable pageable);

    List<Intern> findByMentorId(UUID mentorId);

    /**
     * Find intern by id and mentor_id
     * This is to ensure that the intern belongs to the mentor
     */
    Optional<Intern> findByIdAndMentorId(Integer id, UUID mentorId);

    /**
     * Find all active interns by mentor_id
     * An active intern is defined as an intern with status 'ACTIVE'
     */
    @Query("SELECT i FROM Intern i WHERE i.mentor.id = :mentorId AND i.status = 'ACTIVE'")
    List<Intern> findActiveInternsByMentorId(@Param("mentorId") UUID mentorId);
}
