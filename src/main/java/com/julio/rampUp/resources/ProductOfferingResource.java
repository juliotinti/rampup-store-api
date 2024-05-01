package com.julio.rampUp.resources;

import java.net.URI;
import java.util.ArrayList;
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

import com.julio.rampUp.entities.ProductOffering;
import com.julio.rampUp.services.ProductOfferingService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/productOfferings")
public class ProductOfferingResource {

    @Autowired
    private ProductOfferingService service;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Operator')")
    public ResponseEntity<List<ProductOffering>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Operator')")
    public ResponseEntity<ProductOffering> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(service.findById(id));
    }

    @PostMapping
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<ProductOffering> insert(@Valid @RequestBody ProductOffering productOffering) {
        productOffering = service.insert(productOffering);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(productOffering.getId())
                .toUri();
        return ResponseEntity.created(uri).body(productOffering);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/quantity")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<Integer>> prodQuantity() {
        List<Integer> prodInfo = new ArrayList<>();
        prodInfo.add(service.prodQuantity());
        prodInfo.add(service.productToSale());
        return ResponseEntity.ok().body(prodInfo);
    }

    @PatchMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<ProductOffering> update(@PathVariable Integer id,
            @Valid @RequestBody ProductOffering productOffering) {

        productOffering = service.update(id, productOffering);
        return ResponseEntity.ok().body(productOffering);
    }

}
