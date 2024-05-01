package com.julio.rampUp.services.exceptions;

public class NotForSaleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotForSaleException(Object id) {
        super("Product Offering is not for sale. Id - " + id);
    }

}
