package com.julio.rampUp.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;

import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "order_tb")
@SQLDelete(sql = "UPDATE order_tb SET deleted=1 WHERE id =?")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    @JsonView(View.Public.class)
    private Instant instant;

    @JsonView(View.Public.class)
    private Boolean deleted = Boolean.FALSE;

    @JsonIgnoreProperties("orders")
    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonView(View.Public.class)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "deliveryAddress_id")
    @JsonView(View.Public.class)
    private Address deliveryAddress;

    @OneToMany(mappedBy = "id.order")
    @JsonView(View.Public.class)
    private Set<OrderItem> items = new HashSet<>();

    public Order() {
    }

    public Order(Integer id, Instant instant, Customer customer, Address deliveryAddress) {
        this.id = id;
        this.instant = instant;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
    }

    public Integer getId() {
        return id;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Set<OrderItem> getOrderItems() {
        return items;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
        Order other = (Order) obj;
        return Objects.equals(id, other.id);
    }

}
