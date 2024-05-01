package com.julio.rampUp.services.exceptions;

public class IdNullException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IdNullException(String type) {
        super("Id of " + type + " must not be null.");
    }

}
