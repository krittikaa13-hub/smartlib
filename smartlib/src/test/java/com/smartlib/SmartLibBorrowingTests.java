package com.smartlib;

import com.smartlib.entity.*;
import com.smartlib.exception.BorrowingLimitExceededException;
import com.smartlib.exception.InvalidReturnException;
import com.smartlib.exception.NoAvailableCopyException;
import com.smartlib.exception.UnauthorizedActionException;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.BorrowingRecordRepository;
import com.smartlib.repository.UserRepository;
import com.smartlib.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot Circulation, Borrowing & Return Test Suite (Stage 4).
 * Verifies checkout claims, return flows, due date calculation, policy quota limits,
 * concurrency/consistency invariants, transaction history retention, and security authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmartLibBorrowingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private BorrowingRecordRepository borrowingRecordRepository;

    private User student1;
    private User student2;
    private Book testBook;
    private BookCopy copy1;
    private BookCopy copy2;

    @BeforeEach
    void setUp() {
        // Create 2 test students
        student1 = userRepository.save(new User("Test Student One", "student1@smartlib.edu", "Password#123", Role.USER));
        student2 = userRepository.save(new User("Test Student Two", "student2@smartlib.edu", "Password#123", Role.USER));

        // Create a test book with 2 copies
        testBook = bookRepository.save(new Book(
                "9780000000001",
                "Discrete Mathematics and Its Applications",
                "Kenneth H. Rosen",
                "McGraw-Hill",
                2019,
                "Mathematics",
                "Textbook on discrete structures, sets, and graph theory.",
                "Stack 1 - Row B - Bay 2",
                2,
                2
        ));

        // Create 2 available physical copies
        copy1 = bookCopyRepository.save(new BookCopy(testBook, "DISC-MATH-C01", CopyStatus.AVAILABLE, testBook.getShelfLocation()));
        copy2 = bookCopyRepository.save(new BookCopy(testBook, "DISC-MATH-C02", CopyStatus.AVAILABLE, testBook.getShelfLocation()));
    }

    // -------------------------------------------------------------------------
    // 1. Successful Borrowing of an Available Copy
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1. Successful borrowing of an available copy claims copy and returns valid record")
    void testSuccessfulBorrowing() {
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());

        assertNotNull(record.getId());
        assertEquals(student1.getId(), record.getUser().getId());
        assertEquals(CopyStatus.BORROWED, record.getBookCopy().getStatus());
        assertEquals(BorrowingStatus.BORROWED, record.getStatus());
        assertNotNull(record.getBorrowedAt());
        assertNotNull(record.getDueDate());
        assertNull(record.getReturnedAt());

        // Verify book copy status in database
        BookCopy updatedCopy = bookCopyRepository.findById(record.getBookCopy().getId()).orElseThrow();
        assertEquals(CopyStatus.BORROWED, updatedCopy.getStatus());

        // Verify parent Book availableCopies count decremented from 2 to 1
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(1, updatedBook.getAvailableCopies());
    }

    // -------------------------------------------------------------------------
    // 2. Rejection When No Copy is Available
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2. Rejection with NoAvailableCopyException when all physical copies are checked out")
    void testRejectionWhenNoCopiesAvailable() {
        // Check out both copies
        borrowingService.borrowBook(student1, testBook.getId());
        borrowingService.borrowBook(student2, testBook.getId());

        // Create third student attempting to borrow
        User student3 = userRepository.save(new User("Student Three", "student3@smartlib.edu", "Pass#123", Role.USER));

        assertThrows(NoAvailableCopyException.class, () -> borrowingService.borrowBook(student3, testBook.getId()));
    }

    // -------------------------------------------------------------------------
    // 3. Prevention of Simultaneous Duplicate Borrowing
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("3. Prevents duplicate simultaneous loan of the same book title by the same user")
    void testPreventDuplicateActiveLoanOfSameTitle() {
        borrowingService.borrowBook(student1, testBook.getId());

        // student1 attempts to borrow the same title again before returning
        assertThrows(IllegalStateException.class, () -> borrowingService.borrowBook(student1, testBook.getId()));
    }

    // -------------------------------------------------------------------------
    // 4. Successful Return by Borrower
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4. Successful return by the borrower marks status RETURNED and sets copy AVAILABLE")
    void testSuccessfulReturnByBorrower() {
        BorrowingRecord borrowRecord = borrowingService.borrowBook(student1, testBook.getId());

        BorrowingRecord returnRecord = borrowingService.returnBook(student1, borrowRecord.getId());

        assertEquals(BorrowingStatus.RETURNED, returnRecord.getStatus());
        assertNotNull(returnRecord.getReturnedAt());

        // Copy is now available again
        BookCopy copy = bookCopyRepository.findById(borrowRecord.getBookCopy().getId()).orElseThrow();
        assertEquals(CopyStatus.AVAILABLE, copy.getStatus());

        // Book available copies restored to 2
        Book book = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(2, book.getAvailableCopies());
    }

    // -------------------------------------------------------------------------
    // 5. Rejection of a Return by Another User
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("5. Rejects return attempt by a different user with UnauthorizedActionException")
    void testRejectionOfReturnByDifferentUser() {
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());

        // student2 tries to return student1's loan
        assertThrows(UnauthorizedActionException.class, () -> borrowingService.returnBook(student2, record.getId()));
    }

    // -------------------------------------------------------------------------
    // 6. Rejection of Repeated Returns
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("6. Rejects repeated return with InvalidReturnException")
    void testRejectionOfRepeatedReturns() {
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());

        // First return succeeds
        borrowingService.returnBook(student1, record.getId());

        // Second return attempt on same record must fail
        assertThrows(InvalidReturnException.class, () -> borrowingService.returnBook(student1, record.getId()));
    }

    // -------------------------------------------------------------------------
    // 7. Correct Due Date Calculation
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("7. Correct due date calculation matches configurable policy (e.g. today + 14 days)")
    void testCorrectDueDateCalculation() {
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());

        LocalDate expectedDueDate = LocalDate.now().plusDays(borrowingService.getLoanDurationDays());
        assertEquals(expectedDueDate, record.getDueDate());
    }

    // -------------------------------------------------------------------------
    // 8. User Borrowing Limit Enforcement
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("8. Enforces user borrowing limit with BorrowingLimitExceededException")
    void testBorrowingLimitEnforcement() {
        int maxAllowed = borrowingService.getMaxLoans();

        // Create enough distinct books to reach and exceed the max loan limit
        for (int i = 1; i <= maxAllowed; i++) {
            Book book = bookRepository.save(new Book(
                    "97800000000" + (10 + i),
                    "Academic Volume " + i,
                    "Author " + i,
                    "Press",
                    2021,
                    "General",
                    "Description " + i,
                    "Stack 1",
                    1,
                    1
            ));
            bookCopyRepository.save(new BookCopy(book, "VOL-" + i + "-C1", CopyStatus.AVAILABLE, "Stack 1"));
            borrowingService.borrowBook(student1, book.getId());
        }

        // Create one more book
        Book extraBook = bookRepository.save(new Book(
                "9780000000999",
                "Extra Over-Limit Book",
                "Extra Author",
                "Press",
                2022,
                "General",
                "Extra Description",
                "Stack 1",
                1,
                1
        ));
        bookCopyRepository.save(new BookCopy(extraBook, "EXTRA-C1", CopyStatus.AVAILABLE, "Stack 1"));

        // Attempting to exceed maxLoans must throw BorrowingLimitExceededException
        assertThrows(BorrowingLimitExceededException.class, () -> borrowingService.borrowBook(student1, extraBook.getId()));
    }

    // -------------------------------------------------------------------------
    // 9. Preservation of Transaction History After Return
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("9. Preserves completed transaction history in database for audit ledger")
    void testPreservationOfTransactionHistory() {
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());
        borrowingService.returnBook(student1, record.getId());

        List<BorrowingRecord> history = borrowingService.getBorrowingHistoryForUser(student1);
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(r -> r.getId().equals(record.getId()) && r.getStatus() == BorrowingStatus.RETURNED));
    }

    // -------------------------------------------------------------------------
    // 10. Availability Consistency After Borrowing and Returning
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("10. Availability consistency: BookCopy status and Book.availableCopies remain strictly synchronized")
    void testAvailabilityConsistency() {
        // Initial state: 2 copies available
        assertEquals(2, bookCopyRepository.countByBookIdAndStatus(testBook.getId(), CopyStatus.AVAILABLE));
        assertEquals(2, bookRepository.findById(testBook.getId()).orElseThrow().getAvailableCopies());

        // Checkout copy 1
        BorrowingRecord record = borrowingService.borrowBook(student1, testBook.getId());
        assertEquals(1, bookCopyRepository.countByBookIdAndStatus(testBook.getId(), CopyStatus.AVAILABLE));
        assertEquals(1, bookRepository.findById(testBook.getId()).orElseThrow().getAvailableCopies());

        // Return copy 1
        borrowingService.returnBook(student1, record.getId());
        assertEquals(2, bookCopyRepository.countByBookIdAndStatus(testBook.getId(), CopyStatus.AVAILABLE));
        assertEquals(2, bookRepository.findById(testBook.getId()).orElseThrow().getAvailableCopies());
    }

    // -------------------------------------------------------------------------
    // 11. Access Control for Unauthenticated and Admin Routes
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("11. Access control: Unauthenticated access redirects to /login; non-admin receives 403 on /admin/borrowings")
    void testAccessControlForCirculationRoutes() throws Exception {
        // Anonymous user accessing /my-library is redirected to /login
        mockMvc.perform(get("/my-library"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Anonymous user attempting to POST /borrow/1 is redirected to /login
        mockMvc.perform(post("/borrow/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // Authenticated USER is denied access to /admin/borrowings with 403 Forbidden
        mockMvc.perform(get("/admin/borrowings").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("student1@smartlib.edu").roles("USER")))
                .andExpect(status().isForbidden());

        // Authenticated ADMIN is granted access to /admin/borrowings with HTTP 200
        mockMvc.perform(get("/admin/borrowings").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@smartlib.edu").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/borrowings/list"))
                .andExpect(model().attributeExists("records"));
    }
}
