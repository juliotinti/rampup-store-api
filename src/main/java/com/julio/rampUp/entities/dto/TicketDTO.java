package com.julio.rampUp.entities.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

public class TicketDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    @JsonView(View.Public.class)
    private Instant instant;

    @NotBlank(message = "This ticket must have a message")
    @JsonView(View.Public.class)
    private String message;

    @NotNull(message = "This ticket must have a order")
    @JsonView(View.Public.class)
    private Integer orderId;

    public TicketDTO() {

    }

    public TicketDTO(Ticket ticket) {
        this.id = ticket.getId();
        this.instant = Instant.now();
        this.message = ticket.getMessage();
        this.orderId = ticket.getOrder().getId();
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

    public Integer getOrderId() {
        return orderId;
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
        TicketDTO other = (TicketDTO) obj;
        return Objects.equals(id, other.id);
    }

}
