package com.julio.rampUp.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.julio.rampUp.entities.enums.CustomerType;
import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "customer_tb")
@SQLDelete(sql = "UPDATE customer_tb SET deleted=1, customer_Status='isDeleted',"
        + " customer_Type=4, password='null' WHERE id=?")
@Where(clause = "deleted=false")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Integer id;

    @NotBlank(message = "Name is mandatory")
    @JsonView(View.Public.class)
    private String customerName;

    @Positive
    @NotNull(message = "Document Number must not be null")
    @JsonView(View.Public.class)
    private Integer documentNumber;

    @JsonView(View.Public.class)
    private String customerStatus;

    @JsonView(View.Public.class)
    private Integer customerType;

    @NotBlank(message = "Credit score is mandatory")
    @JsonView(View.Public.class)
    private String creditScore;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @JsonIgnore
    private Boolean deleted = Boolean.FALSE;

    @JsonIgnoreProperties("customer")
    @OneToOne
    @JsonView(View.Public.class)
    private User user;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE)
    @Where(clause = "deleted=false")
    @JsonView(View.Public.class)
    private List<Address> addresses = new ArrayList<>();

    @JsonIgnoreProperties("customer")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE)
    @JsonView(View.Public.class)
    private Set<Order> orders = new HashSet<>();

    public Customer() {
    }

    public Customer(Integer id, String customerName, Integer documentNumber, CustomerType customerType,
            String creditScore, String password) {
        this.id = id;
        this.customerName = customerName;
        this.documentNumber = documentNumber;
        this.customerStatus = "Active Customer";
        setCustomerType(customerType);
        this.creditScore = creditScore;
        this.password = password;
    }

    public Integer getId() {
        return id;
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

    public CustomerType getCustomerType() {
        return CustomerType.valueOf(customerType);
    }

    public void setCustomerType(CustomerType customerType) {
        if (customerType != null)
            this.customerType = customerType.getCode();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void addAddress(Address address) {
        addresses.add(address);
    }

    public void addOrder(Order order) {
        orders.add(order);
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
        Customer other = (Customer) obj;
        return Objects.equals(id, other.id);
    }

}
