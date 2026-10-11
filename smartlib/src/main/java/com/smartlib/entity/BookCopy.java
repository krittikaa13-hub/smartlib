package com.smartlib.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an individual physical book copy tracked by copyCode.
 * Allows managing individual physical inventory items separately from the parent Book title.
 */
@Entity
@Table(name = "book_copies", indexes = {
    @Index(name = "idx_copy_code", columnList = "copy_code", unique = true),
    @Index(name = "idx_copy_book_status", columnList = "book_id, status")
})
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "copy_code", nullable = false, unique = true, length = 50)
    private String copyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(name = "shelf_location", length = 100)
    private String shelfLocation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BookCopy() {
    }

    public BookCopy(Book book, String copyCode, CopyStatus status, String shelfLocation) {
        this.book = book;
        this.copyCode = copyCode != null ? copyCode.trim() : null;
        this.status = status != null ? status : CopyStatus.AVAILABLE;
        this.shelfLocation = shelfLocation != null ? shelfLocation.trim() : (book != null ? book.getShelfLocation() : null);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.shelfLocation == null && this.book != null) {
            this.shelfLocation = this.book.getShelfLocation();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode != null ? copyCode.trim() : null;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation != null ? shelfLocation.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
