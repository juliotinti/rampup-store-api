package com.julio.rampUp.entities.enums;

public enum Authorities {
    Operator(1), Admin(2);

    private int code;

    private Authorities(int code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Authorities valueOf(int code) {
        for (Authorities value : Authorities.values()) {
            if (value.getCode() == code)
                return value;
        }
        throw new IllegalArgumentException("Invalid authorities type code");
    }

}
