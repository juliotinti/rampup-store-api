package com.julio.rampUp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.julio.rampUp.entities.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    Page<Address> findAllByDeleted(Pageable pageable, Boolean deleted);

    @Query("SELECT a FROM Address a WHERE a.customer.id=?1 AND a.deleted=false")
    Page<Address> findAllAddressesByCustomerId(Pageable pageable, Integer id);
}
