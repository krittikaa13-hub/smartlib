package com.smartlib.exception;

/**
 * Thrown when a return request is invalid, such as returning a record that is
 * already returned or does not exist.
 */
public class InvalidReturnException extends RuntimeException {

    public InvalidReturnException(String message) {
        super(message);
    }
}
