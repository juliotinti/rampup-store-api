package com.julio.rampUp.services.exceptions;

public class EmailNullException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public EmailNullException() {
		super("There is no email");
	}
}
