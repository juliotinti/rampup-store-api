package com.julio.rampUp.entities.enums;

public enum AddressType {
    HomeAddress(1), BusinessAddress(2), ShippingAddress(3);

    private int code;

    private AddressType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AddressType valueOf(int code) {
        for (AddressType value : AddressType.values()) {
            if (value.getCode() == code)
                return value;
        }
        throw new IllegalArgumentException("Invalid address type code");
    }

}
