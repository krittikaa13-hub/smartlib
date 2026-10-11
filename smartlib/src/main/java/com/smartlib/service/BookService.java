package com.smartlib.service;

import com.smartlib.dto.BookDto;
import com.smartlib.entity.Book;
import com.smartlib.exception.DuplicateIsbnException;
import com.smartlib.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing library book catalogue operations, search queries,
 * copy invariant enforcement, and administrative inventory controls.
 */
@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Retrieves a page of active books with optional keyword search and category filtering.
     */
    @Transactional(readOnly = true)
    public Page<Book> searchCatalogue(String query, String category, Pageable pageable) {
        boolean hasQuery = query != null && !query.trim().isBlank();
        boolean hasCategory = category != null && !category.trim().isBlank() && !category.equalsIgnoreCase("ALL");

        String trimmedQuery = hasQuery ? query.trim() : null;
        String trimmedCategory = hasCategory ? category.trim() : null;

        if (hasQuery && hasCategory) {
            return bookRepository.searchActiveBooksWithCategory(trimmedQuery, trimmedCategory, pageable);
        } else if (hasQuery) {
            return bookRepository.searchActiveBooks(trimmedQuery, pageable);
        } else if (hasCategory) {
            return bookRepository.findByActiveTrueAndCategoryIgnoreCase(trimmedCategory, pageable);
        } else {
            return bookRepository.findByActiveTrue(pageable);
        }
    }

    /**
     * Retrieves an active book by ID for public details view.
     */
    @Transactional(readOnly = true)
    public Optional<Book> getActiveBookById(Long id) {
        return bookRepository.findByIdAndActiveTrue(id);
    }

    /**
     * Retrieves any book by ID (including inactive) for administrative management.
     */
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Lists distinct categories present across active books.
     */
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return bookRepository.findDistinctCategories();
    }

    /**
     * Admin view: lists all books (active and inactive) with optional search.
     */
    @Transactional(readOnly = true)
    public Page<Book> getAllBooksForAdmin(String query, Pageable pageable) {
        if (query != null && !query.trim().isBlank()) {
            return bookRepository.searchAllBooksAdmin(query.trim(), pageable);
        }
        return bookRepository.findAll(pageable);
    }

    /**
     * Creates a new book record with duplicate ISBN validation and copy count checks.
     */
    @Transactional
    public Book createBook(BookDto dto) {
        validateCopyCounts(dto.getTotalCopies(), dto.getAvailableCopies());

        String isbn = dto.getIsbn() != null && !dto.getIsbn().isBlank() ? dto.getIsbn().trim() : null;
        if (isbn != null && bookRepository.existsByIsbnIgnoreCase(isbn)) {
            log.warn("Book creation rejected: ISBN '{}' already exists", isbn);
            throw new DuplicateIsbnException("A book with ISBN '" + isbn + "' already exists in the catalogue.");
        }

        Book book = new Book(
                isbn,
                dto.getTitle(),
                dto.getAuthor(),
                dto.getPublisher(),
                dto.getPublicationYear(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getShelfLocation(),
                dto.getTotalCopies(),
                dto.getAvailableCopies()
        );
        book.setActive(dto.isActive());

        Book savedBook = bookRepository.save(book);
        log.info("Book successfully created: id={}, title='{}', isbn='{}'",
                savedBook.getId(), savedBook.getTitle(), savedBook.getIsbn());
        return savedBook;
    }

    /**
     * Updates an existing book record.
     */
    @Transactional
    public Book updateBook(Long id, BookDto dto) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));

        validateCopyCounts(dto.getTotalCopies(), dto.getAvailableCopies());

        String isbn = dto.getIsbn() != null && !dto.getIsbn().isBlank() ? dto.getIsbn().trim() : null;
        if (isbn != null && bookRepository.existsByIsbnIgnoreCaseAndIdNot(isbn, id)) {
            log.warn("Book update rejected: ISBN '{}' already exists on another book", isbn);
            throw new DuplicateIsbnException("Another book already holds ISBN '" + isbn + "'.");
        }

        existing.setIsbn(isbn);
        existing.setTitle(dto.getTitle());
        existing.setAuthor(dto.getAuthor());
        existing.setPublisher(dto.getPublisher());
        existing.setPublicationYear(dto.getPublicationYear());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setShelfLocation(dto.getShelfLocation());
        existing.setTotalCopies(dto.getTotalCopies());
        existing.setAvailableCopies(dto.getAvailableCopies());
        existing.setActive(dto.isActive());

        Book updatedBook = bookRepository.save(existing);
        log.info("Book updated: id={}, title='{}'", updatedBook.getId(), updatedBook.getTitle());
        return updatedBook;
    }

    /**
     * Safely toggles book deactivation/activation without hard-deleting the record,
     * preserving any past or active borrowing history.
     */
    @Transactional
    public Book toggleBookActiveStatus(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));

        book.setActive(!book.isActive());
        Book saved = bookRepository.save(book);
        log.info("Book status toggled: id={}, active={}", saved.getId(), saved.isActive());
        return saved;
    }

    /**
     * Business validation helper for copy counts.
     */
    private void validateCopyCounts(Integer total, Integer available) {
        if (total == null || total < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative.");
        }
        if (available == null || available < 0) {
            throw new IllegalArgumentException("Available copies cannot be negative.");
        }
        if (available > total) {
            throw new IllegalArgumentException("Available copies (" + available + ") cannot exceed total copies (" + total + ").");
        }
    }

    // Metrics
    @Transactional(readOnly = true)
    public long getActiveTitlesCount() {
        return bookRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public long getTotalCopiesCount() {
        return bookRepository.sumTotalCopies();
    }

    @Transactional(readOnly = true)
    public long getAvailableCopiesCount() {
        return bookRepository.sumAvailableCopies();
    }
}
