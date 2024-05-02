package com.julio.rampUp.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.SQLDelete;

import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "ticket_tb")
@SQLDelete(sql = "UPDATE ticket_tb SET solved=true WHERE id =?")
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    @JsonView(View.Public.class)
    private Instant instant;

    @NotBlank(message = "This ticket must have a message")
    @JsonView(View.Public.class)
    private String message;

    @JsonView(View.Public.class)
    private Boolean solved = Boolean.FALSE;

    @OneToOne
    @JoinColumn(name = "order_id")
    @JsonView(View.Public.class)
    private Order order;

    private Integer customerId;

    public Ticket() {
    }

    public Ticket(Integer id, String message, Order order) {
        this.id = id;
        this.instant = Instant.now();
        this.message = message;
        this.order = order;
        this.customerId = order.getCustomer().getId();
    }

    public Integer getId() {
        return id;
    }

    public Instant getInstant() {
        return instant;
    }

    public String getMessage() {
        return message;
    }

    public Order getOrder() {
        return order;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    public Integer getCustomerId() {
        return customerId;
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
        Ticket other = (Ticket) obj;
        return Objects.equals(id, other.id);
    }

}
