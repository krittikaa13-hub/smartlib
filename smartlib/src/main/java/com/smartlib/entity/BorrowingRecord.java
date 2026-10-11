package com.smartlib.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity representing an immutable circulation transaction record.
 * Tracks custody of a physical book copy, loan dates, due dates, and return completion.
 */
@Entity
@Table(name = "borrowing_records", indexes = {
    @Index(name = "idx_borrow_user_status", columnList = "user_id, status"),
    @Index(name = "idx_borrow_copy_status", columnList = "book_copy_id, status"),
    @Index(name = "idx_borrow_due_date", columnList = "due_date")
})
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowingStatus status = BorrowingStatus.BORROWED;

    public BorrowingRecord() {
    }

    public BorrowingRecord(User user, BookCopy bookCopy, LocalDateTime borrowedAt, LocalDate dueDate) {
        this.user = user;
        this.bookCopy = bookCopy;
        this.borrowedAt = borrowedAt != null ? borrowedAt : LocalDateTime.now();
        this.dueDate = dueDate;
        this.status = BorrowingStatus.BORROWED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.borrowedAt == null) {
            this.borrowedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = BorrowingStatus.BORROWED;
        }
    }

    /**
     * Checks if this active loan is overdue past the due date.
     */
    public boolean isOverdue() {
        if (this.status != BorrowingStatus.BORROWED || this.dueDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate);
    }

    /**
     * Calculates days remaining until due date, or negative days if overdue.
     */
    public long getDaysRemaining() {
        if (this.dueDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }
}
