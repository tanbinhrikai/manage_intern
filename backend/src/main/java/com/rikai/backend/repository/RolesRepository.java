package com.rikai.backend.repository;

import com.rikai.backend.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository  extends JpaRepository<Roles , String> {

    Optional<Roles> findByRoleName(String roleName);
}
