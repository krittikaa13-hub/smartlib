package com.smartlib.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Book entity representing physical and digital library titles.
 * Enforces copy count non-negativity and consistency.
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_book_isbn", columnList = "isbn"),
    @Index(name = "idx_book_title", columnList = "title"),
    @Index(name = "idx_book_author", columnList = "author"),
    @Index(name = "idx_book_category", columnList = "category")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 200)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "shelf_location", nullable = false, length = 100)
    private String shelfLocation;

    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Book() {
    }

    public Book(String isbn, String title, String author, String publisher,
                Integer publicationYear, String category, String description,
                String shelfLocation, Integer totalCopies, Integer availableCopies) {
        this.isbn = isbn != null ? isbn.trim() : null;
        this.title = title != null ? title.trim() : null;
        this.author = author != null ? author.trim() : null;
        this.publisher = publisher != null ? publisher.trim() : null;
        this.publicationYear = publicationYear;
        this.category = category != null ? category.trim() : null;
        this.description = description;
        this.shelfLocation = shelfLocation != null ? shelfLocation.trim() : null;
        this.totalCopies = totalCopies != null ? totalCopies : 1;
        this.availableCopies = availableCopies != null ? availableCopies : this.totalCopies;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        validateCopyCounts();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        validateCopyCounts();
    }

    /**
     * Validates that totalCopies and availableCopies adhere to library invariants:
     * availableCopies >= 0 and availableCopies <= totalCopies.
     */
    public void validateCopyCounts() {
        if (this.totalCopies == null || this.totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative.");
        }
        if (this.availableCopies == null || this.availableCopies < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative.");
        }
        if (this.availableCopies > this.totalCopies) {
            throw new IllegalArgumentException("Available copies (" + this.availableCopies + 
                    ") cannot exceed total copies (" + this.totalCopies + ").");
        }
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn != null && !isbn.isBlank() ? isbn.trim() : null; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title.trim() : null; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author != null ? author.trim() : null; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher != null ? publisher.trim() : null; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category != null ? category.trim() : null; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation != null ? shelfLocation.trim() : null; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
