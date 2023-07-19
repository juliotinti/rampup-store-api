package com.ericsson.rampUp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ericsson.rampUp.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Override
    Page<Customer> findAll(Pageable pageable);
}
