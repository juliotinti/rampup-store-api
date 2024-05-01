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

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.Order;
import com.julio.rampUp.entities.Ticket;
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.entities.dto.CustomerDTO;
import com.julio.rampUp.services.AddressService;
import com.julio.rampUp.services.CustomerService;
import com.julio.rampUp.services.OrderService;
import com.julio.rampUp.services.TicketService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/customers")
public class CustomerResource {

    @Autowired
    private CustomerService service;

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TicketService ticketService;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<CustomerDTO>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/home/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Customer> findById(@PathVariable Integer id) {
        Customer customer = service.findById(id);
        return ResponseEntity.ok().body(customer);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Operator')")
    public ResponseEntity<Customer> insert(@Valid @RequestBody CustomerDTO customerDTO) {
        Customer customer = service.insert(customerDTO);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(customer.getId())
                .toUri();
        return ResponseEntity.created(uri).body(customer);
    }

    @PatchMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Customer> update(@PathVariable Integer id, @RequestBody Customer customer) {
        customer = service.update(id, customer);
        return ResponseEntity.ok().body(customer);
    }

    @GetMapping(value = "/{id}/addresses/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<List<Address>> findAllAddressesByCustomerId(@PathVariable Integer id,
            @PathVariable int page) {
        return ResponseEntity.ok().body(addressService.findAllAddressesByCustomerId(page, id));
    }

    @GetMapping(value = "/{id}/address/id/{address_id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Address> findAddressById(@PathVariable Integer id, @PathVariable int address_id) {
        return ResponseEntity.ok().body(addressService.findById(address_id));
    }

    @DeleteMapping(value = "/{id}/addresses/{addressId}")
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Void> deleteAddressById(@PathVariable Integer id, @PathVariable Integer addressId) {
        addressService.deleteById(id, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/addresses/")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Address> insertAddress(@PathVariable Integer id, @Valid @RequestBody AddressDTO addressDTO) {
        Address address = addressService.insert(id, addressDTO);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/addresses/")
                .buildAndExpand(address.getId()).toUri();
        return ResponseEntity.created(uri).body(address);
    }

    @PatchMapping(value = "/{id}/addresses/{addressId}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<Address> updateAddress(@PathVariable Integer id, @PathVariable Integer addressId,
            @Valid @RequestBody Address address) {
        address = addressService.update(id, addressId, address);
        return ResponseEntity.ok().body(address);
    }

    @GetMapping(value = "/{id}/orders/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<List<Order>> findAllOrdersByCustomerId(@PathVariable Integer id, @PathVariable int page) {
        return ResponseEntity.ok().body(orderService.findAllOrdersByCustomerId(page, id));
    }

    @GetMapping(value = "/{id}/tickets/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @customerRepository.findById(#id).get().getUser()"
            + ".getEmail()")
    public ResponseEntity<List<Ticket>> findAllTicketsByCustomerId(@PathVariable Integer id, @PathVariable int page) {
        return ResponseEntity.ok().body(ticketService.findAllTicketsByCustomerId(page, id));
    }

}
