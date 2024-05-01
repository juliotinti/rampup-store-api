package com.julio.rampUp.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.julio.rampUp.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Override
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customer.id = ?1 AND o.deleted=0 ORDER BY o.id DESC")
    Page<Order> findAllOrdersByCustomerId(Pageable pageable, Integer id);

    @Query("SELECT count(*) FROM Order o WHERE o.deleted=0")
    Integer quantityOfOrders();

    @Query("SELECT count(*) FROM Order o WHERE o.deleted=1")
    Integer quantityOfCancelledOrders();

}
