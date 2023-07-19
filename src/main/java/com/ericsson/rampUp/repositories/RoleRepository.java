package com.ericsson.rampUp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ericsson.rampUp.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Override
    Page<Role> findAll(Pageable pageable);
}
