package com.julio.rampUp.services.exceptions;

public class NoValueForIdException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoValueForIdException(String type, Object id) {
        super("There is no " + type + " using this id (" + id + ")");
    }

    public NoValueForIdException(String type, String type2, Object idType1, Object idType2) {
        super("There is no " + type + " using this id (" + idType1 + "). Or, there is no " + type2 + " using this id ("
                + idType2 + ")");
    }

}
