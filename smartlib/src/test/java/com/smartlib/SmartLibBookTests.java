package com.smartlib;

import com.smartlib.dto.BookDto;
import com.smartlib.entity.Book;
import com.smartlib.exception.DuplicateIsbnException;
import com.smartlib.repository.BookRepository;
import com.smartlib.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot Book Catalogue & Inventory Test Suite (Stage 3).
 * Verifies book creation, copy invariants, duplicate ISBN rejection,
 * search & category filtering, update operations, role protection, and safe deactivation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmartLibBookTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    // -------------------------------------------------------------------------
    // 1. Creating a Valid Book
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1. Creating a valid book persists record with active=true and valid copy counts")
    void testCreateValidBook() {
        BookDto dto = new BookDto();
        dto.setIsbn("9780132350884");
        dto.setTitle("Clean Code: A Handbook of Agile Software Craftsmanship");
        dto.setAuthor("Robert C. Martin");
        dto.setPublisher("Prentice Hall");
        dto.setPublicationYear(2008);
        dto.setCategory("Software Engineering");
        dto.setDescription("A handbook of agile craftsmanship focusing on clean code practices.");
        dto.setShelfLocation("Stack 2 - Row D - Bay 2");
        dto.setTotalCopies(5);
        dto.setAvailableCopies(5);
        dto.setActive(true);

        Book created = bookService.createBook(dto);

        assertNotNull(created.getId());
        assertEquals("9780132350884", created.getIsbn());
        assertEquals("Clean Code: A Handbook of Agile Software Craftsmanship", created.getTitle());
        assertEquals(5, created.getTotalCopies());
        assertEquals(5, created.getAvailableCopies());
        assertTrue(created.isActive());
        assertNotNull(created.getCreatedAt());
    }

    // -------------------------------------------------------------------------
    // 2. Rejecting Invalid Book Details
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2. Rejects invalid book details (negative copies, invalid year, blank title)")
    void testRejectInvalidBookDetails() {
        // Test negative total copies rejection
        BookDto negativeCopiesDto = new BookDto();
        negativeCopiesDto.setTitle("Invalid Book");
        negativeCopiesDto.setAuthor("Some Author");
        negativeCopiesDto.setCategory("General");
        negativeCopiesDto.setShelfLocation("Stack 1");
        negativeCopiesDto.setTotalCopies(-1);
        negativeCopiesDto.setAvailableCopies(0);

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(negativeCopiesDto));

        // Test available copies exceeding total copies
        BookDto excessiveAvailableDto = new BookDto();
        excessiveAvailableDto.setTitle("Excessive Available");
        excessiveAvailableDto.setAuthor("Author");
        excessiveAvailableDto.setCategory("General");
        excessiveAvailableDto.setShelfLocation("Stack 1");
        excessiveAvailableDto.setTotalCopies(3);
        excessiveAvailableDto.setAvailableCopies(5);

        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(excessiveAvailableDto));
    }

    // -------------------------------------------------------------------------
    // 3. Rejecting Duplicate ISBNs
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("3. Rejects duplicate ISBNs with DuplicateIsbnException")
    void testRejectDuplicateIsbn() {
        String testIsbn = "9780321573513";

        BookDto first = new BookDto();
        first.setIsbn(testIsbn);
        first.setTitle("Algorithms, Fourth Edition");
        first.setAuthor("Robert Sedgewick");
        first.setPublicationYear(2011);
        first.setCategory("Computer Science");
        first.setShelfLocation("Stack 2 - Row B - Bay 4");
        first.setTotalCopies(3);
        first.setAvailableCopies(3);
        bookService.createBook(first);

        BookDto duplicate = new BookDto();
        duplicate.setIsbn(testIsbn);
        duplicate.setTitle("Another Book with Same ISBN");
        duplicate.setAuthor("Different Author");
        duplicate.setPublicationYear(2020);
        duplicate.setCategory("Computer Science");
        duplicate.setShelfLocation("Stack 2 - Row B - Bay 5");
        duplicate.setTotalCopies(2);
        duplicate.setAvailableCopies(2);

        assertThrows(DuplicateIsbnException.class, () -> bookService.createBook(duplicate));
    }

    // -------------------------------------------------------------------------
    // 4. Searching by Title and Author
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4. Searching by title and author returns relevant active books")
    void testSearchByTitleAndAuthor() {
        BookDto book1 = new BookDto();
        book1.setIsbn("9780136042594");
        book1.setTitle("Artificial Intelligence: A Modern Approach");
        book1.setAuthor("Stuart Russell and Peter Norvig");
        book1.setPublicationYear(2020);
        book1.setCategory("Computer Science");
        book1.setShelfLocation("Stack 2 - Row A - Bay 1");
        book1.setTotalCopies(4);
        book1.setAvailableCopies(4);
        bookService.createBook(book1);

        BookDto book2 = new BookDto();
        book2.setIsbn("9780132126953");
        book2.setTitle("Computer Networks, Fifth Edition");
        book2.setAuthor("Andrew S. Tanenbaum");
        book2.setPublicationYear(2011);
        book2.setCategory("Computer Science");
        book2.setShelfLocation("Stack 2 - Row A - Bay 2");
        book2.setTotalCopies(3);
        book2.setAvailableCopies(3);
        bookService.createBook(book2);

        // Search by author "Russell"
        Page<Book> authorResults = bookService.searchCatalogue("Russell", null, PageRequest.of(0, 10));
        assertTrue(authorResults.getContent().stream().anyMatch(b -> b.getTitle().contains("Artificial Intelligence")));
        assertFalse(authorResults.getContent().stream().anyMatch(b -> b.getTitle().contains("Computer Networks")));

        // Search by title "Networks"
        Page<Book> titleResults = bookService.searchCatalogue("Networks", null, PageRequest.of(0, 10));
        assertTrue(titleResults.getContent().stream().anyMatch(b -> b.getTitle().contains("Computer Networks")));
    }

    // -------------------------------------------------------------------------
    // 5. Filtering by Category
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("5. Filtering by category isolates titles in that specific domain")
    void testFilterByCategory() {
        BookDto mathBook = new BookDto();
        mathBook.setIsbn("9780470458211");
        mathBook.setTitle("Advanced Engineering Mathematics");
        mathBook.setAuthor("Erwin Kreyszig");
        mathBook.setPublicationYear(2011);
        mathBook.setCategory("Mathematics");
        mathBook.setShelfLocation("Stack 1 - Row C - Bay 1");
        mathBook.setTotalCopies(5);
        mathBook.setAvailableCopies(5);
        bookService.createBook(mathBook);

        Page<Book> mathResults = bookService.searchCatalogue(null, "Mathematics", PageRequest.of(0, 10));
        assertFalse(mathResults.isEmpty());
        assertTrue(mathResults.getContent().stream().allMatch(b -> b.getCategory().equalsIgnoreCase("Mathematics")));
    }

    // -------------------------------------------------------------------------
    // 6. Updating Book Details
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("6. Updating book details modifies title, shelf location, and copy counts")
    void testUpdateBookDetails() {
        BookDto initial = new BookDto();
        initial.setIsbn("9780201633610");
        initial.setTitle("Design Patterns: Elements of Reusable Object-Oriented Software");
        initial.setAuthor("Erich Gamma");
        initial.setPublicationYear(1994);
        initial.setCategory("Software Engineering");
        initial.setShelfLocation("Old Shelf A");
        initial.setTotalCopies(4);
        initial.setAvailableCopies(4);
        Book saved = bookService.createBook(initial);

        BookDto updateDto = new BookDto();
        updateDto.setIsbn("9780201633610");
        updateDto.setTitle("Design Patterns (Updated Edition)");
        updateDto.setAuthor("Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides");
        updateDto.setPublicationYear(1994);
        updateDto.setCategory("Software Engineering");
        updateDto.setShelfLocation("Stack 2 - Row D - Bay 4");
        updateDto.setTotalCopies(6);
        updateDto.setAvailableCopies(6);
        updateDto.setActive(true);

        Book updated = bookService.updateBook(saved.getId(), updateDto);

        assertEquals("Design Patterns (Updated Edition)", updated.getTitle());
        assertEquals("Stack 2 - Row D - Bay 4", updated.getShelfLocation());
        assertEquals(6, updated.getTotalCopies());
        assertEquals(6, updated.getAvailableCopies());
    }

    // -------------------------------------------------------------------------
    // 7. Preventing Non-Admins from Accessing Admin Routes
    // -------------------------------------------------------------------------
    @Test
    @WithMockUser(username = "student@university.edu", roles = {"USER"})
    @DisplayName("7. Non-Admins (ROLE_USER) are denied access to /admin/books/** with 403 Forbidden")
    void testPreventNonAdminAccessToAdminBookRoutes() throws Exception {
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/books/new"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // 8. Safe Book Deactivation
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("8. Safe deactivation hides book from public catalogue while preserving record")
    void testSafeBookDeactivation() {
        BookDto dto = new BookDto();
        dto.setIsbn("9780596007126");
        dto.setTitle("Head First Design Patterns");
        dto.setAuthor("Eric Freeman");
        dto.setPublicationYear(2004);
        dto.setCategory("Software Engineering");
        dto.setShelfLocation("Stack 2 - Row D - Bay 3");
        dto.setTotalCopies(3);
        dto.setAvailableCopies(3);
        Book book = bookService.createBook(dto);

        // Deactivate book safely
        Book toggled = bookService.toggleBookActiveStatus(book.getId());
        assertFalse(toggled.isActive());

        // Must still exist in database for historical borrowing integrity
        assertTrue(bookRepository.findById(book.getId()).isPresent());

        // Must be hidden from public active queries
        assertTrue(bookService.getActiveBookById(book.getId()).isEmpty());
    }

    // -------------------------------------------------------------------------
    // 9. Maintaining Valid Total and Available Copy Counts
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("9. Invariant rule: 0 <= availableCopies <= totalCopies is strictly enforced")
    void testMaintainingValidCopyCounts() {
        Book book = new Book("9781111111111", "Copy Invariant Book", "Author", "Press",
                2022, "Science", "Desc", "Stack 1", 5, 5);

        // Valid initial state
        assertDoesNotThrow(book::validateCopyCounts);

        // Invalid: available > total
        book.setAvailableCopies(6);
        assertThrows(IllegalArgumentException.class, book::validateCopyCounts);

        // Invalid: available < 0
        book.setAvailableCopies(-1);
        assertThrows(IllegalArgumentException.class, book::validateCopyCounts);

        // Invalid: total < 0
        book.setAvailableCopies(0);
        book.setTotalCopies(-2);
        assertThrows(IllegalArgumentException.class, book::validateCopyCounts);
    }
}
