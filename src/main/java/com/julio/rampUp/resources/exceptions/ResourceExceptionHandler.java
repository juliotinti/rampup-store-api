package com.julio.rampUp.resources.exceptions;

import java.time.Instant;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.julio.rampUp.services.exceptions.AddressException;
import com.julio.rampUp.services.exceptions.CustomerAlreadyExists;
import com.julio.rampUp.services.exceptions.EmailDuplicateException;
import com.julio.rampUp.services.exceptions.EmailNullException;
import com.julio.rampUp.services.exceptions.IdNullException;
import com.julio.rampUp.services.exceptions.InvalidEmailException;
import com.julio.rampUp.services.exceptions.NoValueForIdException;
import com.julio.rampUp.services.exceptions.NotForSaleException;
import com.julio.rampUp.services.exceptions.ResourceNotFoundException;
import com.julio.rampUp.services.exceptions.UnexpectedException;

//Exception handling with RESTful api, to write the error in postman
@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        String error = "Resource not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NoValueForIdException.class)
    public ResponseEntity<StandardError> noValueForIdException(NoValueForIdException e, HttpServletRequest request) {
        String error = "No value for ID error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(IdNullException.class)
    public ResponseEntity<StandardError> idNullException(IdNullException e, HttpServletRequest request) {
        String error = "ID of a reference is null";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(EmailDuplicateException.class)
    public ResponseEntity<StandardError> emailDuplicateException(EmailDuplicateException e,
            HttpServletRequest request) {
        String error = "Duplicate email.";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(EmailNullException.class)
    public ResponseEntity<StandardError> emailNullException(EmailNullException e, HttpServletRequest request) {
        String error = "Whithout email";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<StandardError> invalidEmailException(InvalidEmailException e, HttpServletRequest request) {
        String error = "Invalid email";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<StandardError> addressException(AddressException e, HttpServletRequest request) {
        String error = "Invalid address";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<StandardError> unexpectedException(UnexpectedException e, HttpServletRequest request) {
        String error = "Unexpected Exception";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<StandardError> noSuchElementException(NoSuchElementException e, HttpServletRequest request) {
        String error = "No value present in this id";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<StandardError> nullPointerException(NullPointerException e, HttpServletRequest request) {
        String error = "Null pointer exception";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(CustomerAlreadyExists.class)
    public ResponseEntity<StandardError> customerAlreadyExists(CustomerAlreadyExists e, HttpServletRequest request) {
        String error = "Customer already exists.";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NotForSaleException.class)
    public ResponseEntity<StandardError> notForSaleException(NotForSaleException e, HttpServletRequest request) {
        String error = "Product not for sale.";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> methodArgumentNotValidException(MethodArgumentNotValidException e,
            HttpServletRequest request) {
        String error = "Argument non valid";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(status).body(err);
    }

}
