package com.julio.rampUp.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.julio.rampUp.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query(value = "SELECT po.id, po.product_name as prodname, SUM(o.quantity) as quantity "
            + "FROM order_item_tb o JOIN product_offering_tb po ON o.items_id = po.id "
            + "GROUP BY po.product_name, po.id", nativeQuery = true)
    List<Object[]> quantitySoldItems();

    @Query(value = "SELECT po.id,po.product_name as prodname, 0 as quantity "
            + "FROM order_item_tb o JOIN product_offering_tb po ON o.items_id != po.id "
            + "WHERE po.id NOT IN (?1) GROUP BY po.product_name, po.id", nativeQuery = true)
    List<Object[]> quantityNotSoldItems(List<Integer> soldPOIds);

}
