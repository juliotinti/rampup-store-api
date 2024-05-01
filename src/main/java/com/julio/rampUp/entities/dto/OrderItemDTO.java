package com.julio.rampUp.entities.dto;

import java.io.Serializable;

import com.julio.rampUp.entities.OrderItem;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class OrderItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Public.class)
    private Integer productId;
    @JsonView(View.Public.class)
    private Double discount;
    @JsonView(View.Public.class)
    private Integer quantity;

    public OrderItemDTO() {
    }

    public OrderItemDTO(OrderItem orderItem) {
        this.productId = orderItem.getProductOffering().getId();
        this.discount = orderItem.getDiscount();
        this.quantity = orderItem.getQuantity();
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
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

}
