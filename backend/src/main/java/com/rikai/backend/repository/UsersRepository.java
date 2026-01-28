package com.rikai.backend.repository;

import com.rikai.backend.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    @Query("""
                SELECT u FROM Users u
                WHERE (:keyword IS NULL OR :keyword = '' OR 
                      (LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      )
                AND (:startDate IS NULL OR u.dateOfBirth >= :startDate)
                AND (:endDate IS NULL OR u.dateOfBirth <= :endDate)
                AND (:departmentId IS NULL OR u.department.id = :departmentId)
                AND (:isActive IS NULL OR u.isActive = :isActive)
                AND u.role.roleName = 'MENTOR'
            """)
    Page<Users> findAllMentorUsers(
            Pageable pageable,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId);

    @Query("""
                SELECT u FROM Users u
                WHERE (:keyword IS NULL OR :keyword = '' OR 
                      (LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                      )
                AND (:startDate IS NULL OR u.dateOfBirth >= :startDate)
                AND (:endDate IS NULL OR u.dateOfBirth <= :endDate)
                AND (:departmentId IS NULL OR u.department.id = :departmentId)
                AND (:isActive IS NULL OR u.isActive = :isActive)
                AND u.role.roleName = 'HR'
            """)
    Page<Users> findAllHRUsers(
            Pageable pageable,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId);




    long countByRole_RoleName(String roleName);

    long countByDepartment_IdAndRole_RoleName(Long departmentId, String roleName);

    List<Users> findByDepartment_IdAndRole_RoleName(Long departmentId, String roleName);

    Optional<Users> findByEmailAndIsActive(String email, boolean isActive);

    List<Users> findByIsActive(boolean isActive);

    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByDepartment_Id(Long departmentId);
}
