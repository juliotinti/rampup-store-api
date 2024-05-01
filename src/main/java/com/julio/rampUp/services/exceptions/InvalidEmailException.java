package com.julio.rampUp.services.exceptions;

public class InvalidEmailException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public InvalidEmailException() {
		super("This email is invalid");
	}
}
