package com.smartlib.exception;

/**
 * Exception thrown when a book creation or edit introduces an ISBN already held by another book.
 */
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String message) {
        super(message);
    }
}
