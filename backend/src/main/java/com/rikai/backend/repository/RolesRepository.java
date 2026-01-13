package com.rikai.backend.repository;

import com.rikai.backend.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository  extends JpaRepository<Roles , String> {

}
