package com.smartlib.entity;

/**
 * Lifecycle status of a borrowing transaction record.
 * - BORROWED: The physical copy is currently in the custody of the borrower.
 * - RETURNED: The physical copy has been returned to the library stacks.
 */
public enum BorrowingStatus {
    BORROWED,
    RETURNED
}
