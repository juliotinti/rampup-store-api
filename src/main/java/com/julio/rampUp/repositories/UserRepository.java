package com.julio.rampUp.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.julio.rampUp.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.email = ?1")
    Boolean checkEmail(String email);

    @Override
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmail(String email);

}
