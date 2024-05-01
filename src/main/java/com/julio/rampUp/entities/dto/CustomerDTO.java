package com.julio.rampUp.entities.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.julio.rampUp.entities.Customer;
import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class CustomerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Internal.class)
    private Integer id;

    @JsonView(View.Public.class)
    @NotBlank(message = "Name is mandatory")
    private String customerName;

    @Positive
    @NotNull(message = "Document Number must not be null")
    @JsonView(View.Public.class)
    private Integer documentNumber;

    @JsonView(View.Public.class)
    private String customerStatus;

    @JsonView(View.Internal.class)
    private CustomerType customerType;

    @NotBlank(message = "Credit score is mandatory")
    @JsonView(View.Internal.class)
    private String creditScore;

    @NotBlank(message = "Password is mandatory")
    @JsonView(View.Internal.class)
    private String password;

    private Integer userId;

    public CustomerDTO() {
    }

    public CustomerDTO(Customer customer) {
        this.id = customer.getId();
        this.customerName = customer.getCustomerName();
        this.documentNumber = customer.getDocumentNumber();
        this.customerStatus = customer.getCustomerStatus();
        this.customerType = customer.getCustomerType();
        this.creditScore = customer.getCreditScore();
        this.password = customer.getPassword();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Integer documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public String getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(String creditScore) {
        this.creditScore = creditScore;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
