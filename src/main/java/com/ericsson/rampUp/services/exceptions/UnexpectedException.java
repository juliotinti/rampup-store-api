package com.ericsson.rampUp.services.exceptions;

public class UnexpectedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnexpectedException(String msg) {
		super("Unexpected Error: " + msg);
	}
}
