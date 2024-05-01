package com.julio.rampUp.resources;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.julio.rampUp.entities.ItemsSum;
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.dto.OrderDTO;
import com.julio.rampUp.entities.dto.TicketDTO;
import com.julio.rampUp.services.OrderService;
import com.julio.rampUp.services.TicketService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/orders")
public class OrderResource {

    @Autowired
    private OrderService service;

    @Autowired
    private TicketService ticketService;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<OrderDTO>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @orderRepository.findById(#id).get().getCustomer"
            + ".getUser().getEmail()")
    public ResponseEntity<Order> findById(@PathVariable Integer id) {
        Order order = service.findById(id);
        return ResponseEntity.ok().body(order);
    }

    @PostMapping
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Operator')")
    public ResponseEntity<Order> insert(@Valid @RequestBody OrderDTO orderDTO) {
        Order order = service.insert(orderDTO);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(uri).body(order);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Order> update(@PathVariable Integer id, @Valid @RequestBody Order order) {
        order = service.update(id, order);
        return ResponseEntity.ok().body(order);
    }

    @PostMapping(value = "/{id}/ticket")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @orderRepository.findById(#id).get().getCustomer"
            + ".getUser().getEmail()")
    public ResponseEntity<Ticket> createTicket(@PathVariable Integer id, @Valid @RequestBody TicketDTO ticketDTO) {
        Ticket ticket = ticketService.insert(ticketDTO, id);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/ticket").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(ticket);
    }

    @GetMapping(value = "/info")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<Integer>> quantityOfOrders() {
        return ResponseEntity.ok().body(service.ordersInfo());
    }

    @GetMapping(value = "/sold")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<ItemsSum>> selledItems() {
        return ResponseEntity.ok().body(service.quantitySoldItems());
    }

}
