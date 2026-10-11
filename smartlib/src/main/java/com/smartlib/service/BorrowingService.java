package com.smartlib.service;

import com.smartlib.entity.*;
import com.smartlib.exception.BorrowingLimitExceededException;
import com.smartlib.exception.InvalidReturnException;
import com.smartlib.exception.NoAvailableCopyException;
import com.smartlib.exception.UnauthorizedActionException;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.BorrowingRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transactional circulation service managing book checkout claims,
 * return processing, policy enforcement, and copy status synchronization.
 */
@Service
public class BorrowingService {

    private static final Logger log = LoggerFactory.getLogger(BorrowingService.class);

    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BookRepository bookRepository;

    @Value("${smartlib.borrowing.max-loans:3}")
    private int maxLoans;

    @Value("${smartlib.borrowing.loan-duration-days:14}")
    private int loanDurationDays;

    public BorrowingService(BookCopyRepository bookCopyRepository,
                            BorrowingRecordRepository borrowingRecordRepository,
                            BookRepository bookRepository) {
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * Atomically borrows an available physical copy of a book for the authenticated user.
     * Enforces active loan limits, prevents duplicate active title loans, claims copy,
     * and decrements parent book available count.
     */
    @Transactional
    public BorrowingRecord borrowBook(User user, Long bookId) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Authenticated user context is required to borrow books.");
        }

        // 1. Verify user's active loan limit
        long activeLoansCount = borrowingRecordRepository.countByUserIdAndStatus(user.getId(), BorrowingStatus.BORROWED);
        if (activeLoansCount >= maxLoans) {
            log.warn("Borrowing rejected: User '{}' reached max limit of {} loans.", user.getEmail(), maxLoans);
            throw new BorrowingLimitExceededException(
                    "Borrowing limit reached: You currently have " + activeLoansCount +
                    " active loan(s). The maximum allowed under library policy is " + maxLoans + ".");
        }

        // 2. Load Book Title
        Book book = bookRepository.findByIdAndActiveTrue(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book is either inactive or does not exist with ID: " + bookId));

        // 3. Prevent duplicate simultaneous loan of the same book title by the same user
        boolean hasExistingLoan = borrowingRecordRepository.existsByUserIdAndBookIdAndStatus(
                user.getId(), bookId, BorrowingStatus.BORROWED);
        if (hasExistingLoan) {
            log.warn("Borrowing rejected: User '{}' already holds active loan for book '{}'", user.getEmail(), book.getTitle());
            throw new IllegalStateException("You already have an active borrowed copy of '" +
                    book.getTitle() + "'. Please return it before borrowing another copy of the same title.");
        }

        // 4. Claim an available physical copy
        List<BookCopy> availableCopies = bookCopyRepository.findAvailableCopiesForBook(bookId, CopyStatus.AVAILABLE);
        if (availableCopies.isEmpty()) {
            log.warn("Borrowing rejected: No available physical copies for book id={}", bookId);
            throw new NoAvailableCopyException("All physical copies of '" + book.getTitle() +
                    "' are currently checked out. Please check back later.");
        }

        BookCopy copyToBorrow = availableCopies.get(0);

        // 5. Update copy status to BORROWED
        copyToBorrow.setStatus(CopyStatus.BORROWED);
        bookCopyRepository.save(copyToBorrow);

        // 6. Update parent Book availableCopies count to maintain single source of truth
        long remainingAvailable = bookCopyRepository.countByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
        book.setAvailableCopies((int) remainingAvailable);
        bookRepository.save(book);

        // 7. Calculate due date and persist BorrowingRecord
        LocalDate dueDate = LocalDate.now().plusDays(loanDurationDays);
        BorrowingRecord record = new BorrowingRecord(user, copyToBorrow, LocalDateTime.now(), dueDate);
        BorrowingRecord savedRecord = borrowingRecordRepository.save(record);

        log.info("Book successfully borrowed: recordId={}, user='{}', book='{}', copyCode='{}', dueDate={}",
                savedRecord.getId(), user.getEmail(), book.getTitle(), copyToBorrow.getCopyCode(), dueDate);

        return savedRecord;
    }

    /**
     * Processes return of a borrowed copy by the borrower.
     * Enforces ownership, marks record returned, and resets copy to AVAILABLE.
     */
    @Transactional
    public BorrowingRecord returnBook(User user, Long borrowingRecordId) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Authenticated user context is required to return books.");
        }

        BorrowingRecord record = borrowingRecordRepository.findById(borrowingRecordId)
                .orElseThrow(() -> new InvalidReturnException("Borrowing transaction not found with ID: " + borrowingRecordId));

        // Enforce user ownership: users can only return their own loans
        if (!record.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized return attempt: User '{}' tried to return record id={} belonging to user id={}",
                    user.getEmail(), borrowingRecordId, record.getUser().getId());
            throw new UnauthorizedActionException("Security violation: You can only return books borrowed under your own account.");
        }

        // Reject repeated return
        if (record.getStatus() == BorrowingStatus.RETURNED) {
            log.warn("Invalid return attempt: Record id={} has already been returned.", borrowingRecordId);
            throw new InvalidReturnException("This book has already been returned on " + record.getReturnedAt() + ".");
        }

        // Update transaction record
        record.setStatus(BorrowingStatus.RETURNED);
        record.setReturnedAt(LocalDateTime.now());
        BorrowingRecord updatedRecord = borrowingRecordRepository.save(record);

        // Update physical copy status back to AVAILABLE
        BookCopy copy = record.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        // Synchronize parent book availableCopies count
        Book book = copy.getBook();
        long availableCount = bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE);
        book.setAvailableCopies((int) availableCount);
        bookRepository.save(book);

        log.info("Book successfully returned: recordId={}, user='{}', copyCode='{}'",
                updatedRecord.getId(), user.getEmail(), copy.getCopyCode());

        return updatedRecord;
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BorrowingRecord> getActiveLoansForUser(User user) {
        return borrowingRecordRepository.findByUserIdAndStatusOrderByBorrowedAtDesc(user.getId(), BorrowingStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public List<BorrowingRecord> getBorrowingHistoryForUser(User user) {
        return borrowingRecordRepository.findByUserIdOrderByBorrowedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<BorrowingRecord> getAllBorrowingRecordsForAdmin() {
        return borrowingRecordRepository.findAllByOrderByBorrowedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<BorrowingRecord> getOverdueLoansForUser(User user) {
        return borrowingRecordRepository.findOverdueRecordsForUser(user.getId(), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getCopiesForBook(Long bookId) {
        return bookCopyRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public long getActiveLoansCountForUser(User user) {
        return borrowingRecordRepository.countByUserIdAndStatus(user.getId(), BorrowingStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public long getTotalActiveLoansAcrossCampus() {
        return borrowingRecordRepository.countByStatus(BorrowingStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public long getTotalCompletedReturnsAcrossCampus() {
        return borrowingRecordRepository.countByStatus(BorrowingStatus.RETURNED);
    }

    @Transactional(readOnly = true)
    public long getTotalPhysicalCopiesCount() {
        return bookCopyRepository.count();
    }

    public int getMaxLoans() {
        return maxLoans;
    }

    public int getLoanDurationDays() {
        return loanDurationDays;
    }
}
