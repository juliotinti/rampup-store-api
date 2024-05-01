package com.julio.rampUp.services.exceptions;

public class CustomerAlreadyExists extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CustomerAlreadyExists() {
        super("This user already have a customer");
    }
}
