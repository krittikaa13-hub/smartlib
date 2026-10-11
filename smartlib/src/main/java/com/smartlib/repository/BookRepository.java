package com.smartlib.repository;

import com.smartlib.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Book persistence, search queries, and inventory metrics.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Public active book queries
    Page<Book> findByActiveTrue(Pageable pageable);

    Page<Book> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    Optional<Book> findByIdAndActiveTrue(Long id);

    // Search active books across title, author, isbn, or category
    @Query("SELECT b FROM Book b WHERE b.active = true AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<Book> searchActiveBooks(@Param("kw") String keyword, Pageable pageable);

    // Search active books filtered by a specific category
    @Query("SELECT b FROM Book b WHERE b.active = true AND LOWER(b.category) = LOWER(:category) AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<Book> searchActiveBooksWithCategory(@Param("kw") String keyword, @Param("category") String category, Pageable pageable);

    // Admin search across all books (including deactivated)
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<Book> searchAllBooksAdmin(@Param("kw") String keyword, Pageable pageable);

    // ISBN uniqueness checks
    Optional<Book> findByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCaseAndIdNot(String isbn, Long id);

    // Distinct categories for filter dropdown
    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.active = true ORDER BY b.category ASC")
    List<String> findDistinctCategories();

    // Inventory counts
    long countByActiveTrue();

    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM Book b WHERE b.active = true")
    long sumTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM Book b WHERE b.active = true")
    long sumAvailableCopies();
}
