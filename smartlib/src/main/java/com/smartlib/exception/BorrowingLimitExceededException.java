package com.smartlib.exception;

/**
 * Thrown when a user reaches their maximum allowable concurrent active loans.
 */
public class BorrowingLimitExceededException extends RuntimeException {

    public BorrowingLimitExceededException(String message) {
        super(message);
    }
}
