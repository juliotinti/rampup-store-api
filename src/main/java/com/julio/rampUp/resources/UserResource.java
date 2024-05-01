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

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.entities.dto.UserDTO;
import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.services.UserService;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping(value = "/users")
public class UserResource {

    @Autowired
    private UserService service;

    @GetMapping(value = "/page/{page}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<List<UserDTO>> findAll(@PathVariable int page) {
        return ResponseEntity.ok().body(service.findAll(page));
    }

    @GetMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @userRepository.findById(#id).get().getEmail()")
    public ResponseEntity<User> findById(@PathVariable Integer id) {
        return ResponseEntity.ok().body(service.findById(id));
    }

    @PostMapping
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<User> insertAdmin(@Valid @RequestBody User user) {
        user = service.insert(user, new Role(2, Authorities.Admin));
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(user);
    }

    @PostMapping(value = "/signup")
    @JsonView(View.Public.class)
    // anyone can access this endpoint
    public ResponseEntity<User> insertOperator(@Valid @RequestBody User user) {
        user = service.insert(user, new Role(1, Authorities.Operator));
        // to see in postman, the created path
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(uri).body(user);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @userRepository.findById(#id).get().getEmail()")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    @JsonView(View.Public.class)
    @PreAuthorize("hasAuthority('Admin') || authentication.principal == @userRepository.findById(#id).get().getEmail()")
    public ResponseEntity<User> update(@PathVariable Integer id, @Valid @RequestBody User user) {
        user = service.update(id, user);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping(value = "login/{email}")
    @JsonView(View.Public.class)
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(service.findByEmail(email));
    }

}
