package com.julio.rampUp.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.julio.rampUp.entities.pk.OrderItemPk;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "orderItem_tb")
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonView(View.Public.class)
    private OrderItemPk id = new OrderItemPk();

    @JsonView(View.Public.class)
    private Double discount;
    @JsonView(View.Public.class)
    private Integer quantity;
    @JsonView(View.Public.class)
    private Double totalPrice;

    public OrderItem() {
    }

    public OrderItem(Order order, ProductOffering productOffering, Double discount, Integer quantity) {
        this.discount = discount;
        this.quantity = quantity;
        id.setOrder(order);
        id.setProductOffering(productOffering);
        this.totalPrice = (id.getProductOffering().getUnitPrice() * quantity) * (1 - discount);
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @JsonIgnore
    public Order getOrder() {
        return id.getOrder();
    }

    public void setOrder(Order order) {
        id.setOrder(order);
    }

    public ProductOffering getProductOffering() {
        return id.getProductOffering();
    }

    public void setProductOffering(ProductOffering productOffering) {
        id.setProductOffering(productOffering);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderItem other = (OrderItem) obj;
        return Objects.equals(id, other.id);
    }

}
