package com.julio.rampUp.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.SQLDelete;

import com.julio.rampUp.entities.enums.POState;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "product_offering_tb")
@SQLDelete(sql = "UPDATE product_offering_tb SET sell_Indicator=0 WHERE id =?")
public class ProductOffering implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @NotBlank(message = "Product Offering name is mandatory")
    @JsonView(View.Public.class)
    private String productName;

    @NotNull(message = "Unit Price must not be null")
    @JsonView(View.Public.class)
    private Double unitPrice;

    @NotNull(message = "Sell Indicator must be true or false")
    @JsonView(View.Public.class)
    private Boolean sellIndicator;

    @JsonView(View.Public.class)
    private Integer state;

    @OneToMany(mappedBy = "id.productOffering")
    private Set<OrderItem> orderItems = new HashSet<>();

    @JsonIgnore
    private Boolean deleted = Boolean.FALSE;

    public ProductOffering() {
    }

    public ProductOffering(Integer id, String productName, Double unitPrice, Boolean sellIndicator, POState state) {
        this.id = id;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.sellIndicator = sellIndicator;
        setState(state);
    }

    public Integer getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Boolean getSellIndicator() {
        return sellIndicator;
    }

    public void setSellIndicator(Boolean sellIndicator) {
        this.sellIndicator = sellIndicator;
    }

    public POState getState() {
        return POState.valueOf(state);
    }

    public void setState(POState state) {
        this.state = state.getCode();
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonIgnore
    public Set<Order> getOrders() {
        Set<Order> orders = new HashSet<>();
        for (OrderItem item : orderItems) {
            orders.add(item.getOrder());
        }
        return orders;
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
        ProductOffering other = (ProductOffering) obj;
        return Objects.equals(id, other.id);
    }

}
