package com.smartlib.entity;

/**
 * Status enumeration for individual physical book copies.
 * - AVAILABLE: Copy is in the stacks and ready for checkout.
 * - BORROWED: Copy is currently checked out by a library member.
 * - INACTIVE: Copy is lost, undergoing preservation, or withdrawn.
 */
public enum CopyStatus {
    AVAILABLE,
    BORROWED,
    INACTIVE
}
