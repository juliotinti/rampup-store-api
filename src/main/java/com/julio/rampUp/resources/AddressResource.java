package com.julio.rampUp.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.dto.AddressDTO;
import com.julio.rampUp.services.AddressService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/addresses")
public class AddressResource {

    @Autowired
    private AddressService service;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    public ResponseEntity<List<AddressDTO>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/{id}")
    @JsonView(View.Public.class)
    public ResponseEntity<Address> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(service.findById(id));
    }

}
