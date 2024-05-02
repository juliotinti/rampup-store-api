package com.julio.rampUp.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.hibernate.annotations.SQLDelete;

import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "address_tb")
@SQLDelete(sql = "UPDATE address_tb SET deleted=true WHERE id =?")
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @NotBlank(message = "Street is mandatory")
    @JsonView(View.Public.class)
    private String street;

    @NotNull(message = "House number must be a number")
    @Positive(message = "Number must be positive")
    @JsonView(View.Public.class)
    private Integer houseNumber;

    @NotBlank(message = "Neighborhood is mandatory")
    @JsonView(View.Public.class)
    private String neighborhood;

    @NotNull(message = "Zip code must be a number")
    @Positive(message = "Number must be positive")
    @JsonView(View.Public.class)
    private Integer zipCode;

    @NotBlank(message = "Country is mandatory")
    @JsonView(View.Public.class)
    private String country;

    @JsonView(View.Public.class)
    private Integer addressType;

    @JsonIgnore
    private Boolean deleted = Boolean.FALSE;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Address() {
    }

    public Address(Integer id, String street, Integer houseNumber, String neighborhood, Integer zipCode, String country,
            AddressType addressType) {
        this.id = id;
        this.street = street;
        this.houseNumber = houseNumber;
        this.neighborhood = neighborhood;
        this.zipCode = zipCode;
        this.country = country;
        setAddressType(addressType);
    }

    public Integer getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public AddressType getAddressType() {
        return AddressType.valueOf(addressType);
    }

    public void setAddressType(AddressType addressType) {
        if (addressType != null)
            this.addressType = addressType.getCode();
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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
        Address other = (Address) obj;
        return Objects.equals(id, other.id);
    }

}
