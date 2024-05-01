package com.julio.rampUp.entities.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@Embeddable // auxiliar class
public class OrderItemPk implements Serializable {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "items_id")
    @JsonView(View.Public.class)
    private ProductOffering productOffering;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ProductOffering getProductOffering() {
        return productOffering;
    }

    public void setProductOffering(ProductOffering productOffering) {
        this.productOffering = productOffering;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, productOffering);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderItemPk other = (OrderItemPk) obj;
        return Objects.equals(order, other.order) && Objects.equals(productOffering, other.productOffering);
    }

}
