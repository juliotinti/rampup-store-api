package com.julio.rampUp.services.exceptions;

public class EmailDuplicateException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public EmailDuplicateException() {
		super("This email already has a User");
	}
}
