package com.smartlib.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

/**
 * Global Exception Handler for SmartLib.
 * Captures database connection errors and general exceptions with clean user-facing guidance.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public String handleDatabaseException(Exception ex, Model model) {
        log.error("Database connection failure encountered: {}", ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Database Connection Unavailable");
        model.addAttribute("errorMessage", "SmartLib could not establish a connection to MySQL ('smartlib_db'). Please verify that MySQL service is running and that your environment variables (DB_USERNAME, DB_PASSWORD) are set correctly.");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error";
    }

    @ExceptionHandler({NoAvailableCopyException.class, BorrowingLimitExceededException.class, InvalidReturnException.class})
    public String handleBorrowingBusinessException(RuntimeException ex, Model model) {
        log.warn("Borrowing operation rejected: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Borrowing Request Denied");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("technicalDetails", ex.getClass().getSimpleName());
        return "error";
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public String handleUnauthorizedAction(UnauthorizedActionException ex, Model model) {
        log.warn("Security violation: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("technicalDetails", "Security ownership violation");
        return "access-denied";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled application exception: {}", ex.getMessage(), ex);
        model.addAttribute("errorTitle", "Application Error");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request.");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error";
    }
}
