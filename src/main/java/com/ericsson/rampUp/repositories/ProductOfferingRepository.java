package com.ericsson.rampUp.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ericsson.rampUp.entities.ProductOffering;

public interface ProductOfferingRepository extends JpaRepository<ProductOffering, Integer> {
    @Override
    Page<ProductOffering> findAll(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM product_offering_tb t WHERE t.sell_Indicator=1", nativeQuery = true)
    List<Object[]> productToSale();
}
