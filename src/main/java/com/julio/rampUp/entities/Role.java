package com.julio.rampUp.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.julio.rampUp.entities.enums.Authorities;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "role_tb")
@SQLDelete(sql = "UPDATE role_tb SET deleted=1 WHERE id =?")
@Where(clause = "deleted=false")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @JsonView(View.Public.class)
    private Integer authority;

    @JsonIgnore
    private Boolean deleted = Boolean.FALSE;

    public Role() {
    }

    public Role(Integer id, Authorities authority) {
        this.id = id;
        setAuthority(authority);
    }

    public Integer getId() {
        return id;
    }

    public Authorities getAuthority() {
        return Authorities.valueOf(authority);
    }

    public void setAuthority(Authorities authority) {
        if (authority != null)
            this.authority = authority.getCode();
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
        Role other = (Role) obj;
        return Objects.equals(id, other.id);
    }

}
