package com.julio.rampUp.entities.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.julio.rampUp.entities.Role;
import com.julio.rampUp.entities.User;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Public.class)
    private Integer id;
    @JsonView(View.Public.class)
    private String email;
    @JsonView(View.Public.class)
    private Set<Role> roles = new HashSet<>();

    public UserDTO() {
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.roles = user.getRoles();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
