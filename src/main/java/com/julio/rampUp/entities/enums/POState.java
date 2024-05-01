package com.julio.rampUp.entities.enums;

public enum POState {
    Active(1), Definition(2), Technical(3);

    private int code;

    private POState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static POState valueOf(int code) {
        for (POState value : POState.values()) {
            if (value.getCode() == code)
                return value;
        }
        throw new IllegalArgumentException("Invalid POState type code");
    }

}
