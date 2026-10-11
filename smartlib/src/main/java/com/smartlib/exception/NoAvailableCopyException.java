package com.smartlib.exception;

/**
 * Thrown when a user attempts to borrow a book whose copies are all currently
 * checked out or inactive.
 */
public class NoAvailableCopyException extends RuntimeException {

    public NoAvailableCopyException(String message) {
        super(message);
    }
}
