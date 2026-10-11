package com.smartlib.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating and editing books.
 * Enforces server-side validation on title, author, year, and copy counts.
 */
public class BookDto {

    private Long id;

    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)?$|^$", message = "Please enter a valid 10 or 13-digit ISBN (or leave blank)")
    private String isbn;

    @NotBlank(message = "Book title is required")
    @Size(min = 1, max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author name is required")
    @Size(min = 1, max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Size(max = 200, message = "Publisher must not exceed 200 characters")
    private String publisher;

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be 1000 or later")
    @Max(value = 2100, message = "Publication year cannot exceed 2100")
    private Integer publicationYear;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private String description;

    @NotBlank(message = "Shelf location is required (e.g., 'Stack 2 - Row B - Bay 3')")
    @Size(max = 100, message = "Shelf location must not exceed 100 characters")
    private String shelfLocation;

    @NotNull(message = "Total copies count is required")
    @Min(value = 0, message = "Total copies cannot be negative")
    private Integer totalCopies = 1;

    @NotNull(message = "Available copies count is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies = 1;

    private boolean active = true;

    public BookDto() {
    }

    /**
     * Business validation ensuring available copies do not exceed total copies.
     */
    public boolean isCopyCountValid() {
        if (totalCopies == null || availableCopies == null) return false;
        return availableCopies >= 0 && availableCopies <= totalCopies;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn != null && !isbn.isBlank() ? isbn.trim() : null; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
