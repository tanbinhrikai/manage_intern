package com.rikai.backend.repository;


import com.rikai.backend.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    @Query("SELECT u FROM Users u WHERE u.role.roleName = 'MENTOR'")
    Page<Users> findAllMentorUsers(Pageable pageable);

    Optional<Users> findByEmailAndIsActive(String email, boolean isActive);

    List<Users> findByIsActive(boolean isActive);

    Optional<Users> findByEmail(String email);
}
