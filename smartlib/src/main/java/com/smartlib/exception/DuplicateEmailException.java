package com.smartlib.exception;

/**
 * Exception thrown when a user registration attempt uses an already registered email address.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
