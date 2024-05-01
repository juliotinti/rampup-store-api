package com.julio.rampUp.resources;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.services.RoleService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/roles")
public class RoleResource {

    @Autowired
    private RoleService service;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    public ResponseEntity<List<Role>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/{id}")
    @JsonView(View.Public.class)
    public ResponseEntity<Role> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(service.findById(id));
    }

    @PostMapping
    @JsonView(View.Public.class)
    public ResponseEntity<Role> insert(@RequestBody Role role) {
        role = service.insert(role);
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(role.getId()).toUri();
        return ResponseEntity.created(uri).body(role);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    @JsonView(View.Public.class)
    public ResponseEntity<Role> update(@PathVariable Integer id, @RequestBody Role role) {
        role = service.update(id, role);
        return ResponseEntity.ok().body(role);
    }

}
