package com.julio.rampUp.entities.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.julio.rampUp.entities.Address;
import com.julio.rampUp.entities.enums.AddressType;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class AddressDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Internal.class)
    private Integer id;

    @JsonView(View.Public.class)
    @NotBlank(message = "Street must have a value")
    private String street;

    @JsonView(View.Public.class)
    @NotNull(message = "House Number must be a number")
    @Positive(message = "Number must be positive")
    private Integer houseNumber;

    @JsonView(View.Public.class)
    @NotBlank(message = "Neighborhood must have a value")
    private String neighborhood;

    @JsonView(View.Public.class)
    @NotNull(message = "Zip code must be a number")
    @Positive(message = "Number must be positive")
    private Integer zipCode;

    @JsonView(View.Public.class)
    @NotBlank(message = "Country must have a value")
    private String country;

    private AddressType addressType;
    private Integer customerId;

    public AddressDTO() {
    }

    public AddressDTO(Address address) {
        this.id = address.getId();
        this.street = address.getStreet();
        this.houseNumber = address.getHouseNumber();
        this.neighborhood = address.getNeighborhood();
        this.zipCode = address.getZipCode();
        this.country = address.getCountry();
        this.addressType = address.getAddressType();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

}
