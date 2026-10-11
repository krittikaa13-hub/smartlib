package com.smartlib.exception;

/**
 * Thrown when a user attempts an unauthorized operation on a record they do not own,
 * such as returning another student's loan or accessing private borrowing logs.
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
