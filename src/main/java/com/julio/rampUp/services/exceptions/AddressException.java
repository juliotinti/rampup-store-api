package com.julio.rampUp.services.exceptions;

public class AddressException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AddressException() {
        super("This address don't exist for this customer");
    }

    public AddressException(String msg) {
        super(msg);
    }
}
