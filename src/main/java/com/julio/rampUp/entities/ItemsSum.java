package com.julio.rampUp.entities;

import java.io.Serializable;

import com.julio.rampUp.view.View;
import com.fasterxml.jackson.annotation.JsonView;

public class ItemsSum implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonView(View.Public.class)
    private Integer prod_id;
    @JsonView(View.Public.class)
    private String prod_name;
    @JsonView(View.Public.class)
    private Integer quantity;

    public ItemsSum() {
    }

    public ItemsSum(Integer prod_id, Integer quantity, String prod_name) {
        this.prod_id = prod_id;
        this.quantity = quantity;
        this.prod_name = prod_name;
    }

    public Integer getProd_id() {
        return prod_id;
    }

    public void setProd_id(Integer prod_id) {
        this.prod_id = prod_id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProd_name() {
        return prod_name;
    }

    public void setProd_name(String prod_name) {
        this.prod_name = prod_name;
    }

}
