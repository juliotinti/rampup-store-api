package com.julio.rampUp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.julio.rampUp.entities.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    @Query("SELECT t FROM Ticket t WHERE t.solved = ?1")
    Page<Ticket> findAll(Pageable pageable, Boolean solved);

    @Query("SELECT t FROM Ticket t WHERE t.customerId = ?1 ORDER BY t.id DESC")
    Page<Ticket> findAllTicketsByCustomerId(Pageable pageable, Integer id);
}
