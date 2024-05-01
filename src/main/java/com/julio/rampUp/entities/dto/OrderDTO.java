package com.julio.rampUp.entities.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.OrderItem;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Public.class)
    private Integer id;
    @JsonView(View.Public.class)
    @NotNull(message = "Customer is mandatory")
    private Integer customerId;
    @JsonView(View.Public.class)
    @NotNull(message = "Address is mandatory")
    private Integer deliveryId;
    @JsonView(View.Public.class)
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    @JsonView(View.Public.class)
    private Instant moment;

    public OrderDTO() {
    }

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.moment = order.getInstant();
        for (OrderItem orderItem : order.getOrderItems()) {
            this.orderItems.add(new OrderItemDTO(orderItem));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Integer deliveryId) {
        this.deliveryId = deliveryId;
    }

    public List<OrderItemDTO> getOrderItemDTO() {
        return orderItems;
    }

}
