package com.smartlib.repository;

import com.smartlib.entity.BorrowingRecord;
import com.smartlib.entity.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for circulation records and transaction history.
 */
@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    // Active loans for a specific user
    List<BorrowingRecord> findByUserIdAndStatusOrderByBorrowedAtDesc(Long userId, BorrowingStatus status);

    // Full transaction history for a specific user
    List<BorrowingRecord> findByUserIdOrderByBorrowedAtDesc(Long userId);

    // Count of active loans for user limit check
    long countByUserIdAndStatus(Long userId, BorrowingStatus status);

    // Check if user already holds an active loan of this exact book copy
    boolean existsByBookCopyIdAndStatus(Long bookCopyId, BorrowingStatus status);

    // Check if user already has an active loan for any copy of this book title
    @Query("SELECT COUNT(r) > 0 FROM BorrowingRecord r WHERE r.user.id = :userId AND r.bookCopy.book.id = :bookId AND r.status = :status")
    boolean existsByUserIdAndBookIdAndStatus(
            @Param("userId") Long userId,
            @Param("bookId") Long bookId,
            @Param("status") BorrowingStatus status
    );

    // Load record ensuring ownership
    Optional<BorrowingRecord> findByIdAndUserId(Long id, Long userId);

    // Admin view of all transactions
    List<BorrowingRecord> findAllByOrderByBorrowedAtDesc();

    // Overdue loans across system
    @Query("SELECT r FROM BorrowingRecord r WHERE r.status = 'BORROWED' AND r.dueDate < :today ORDER BY r.dueDate ASC")
    List<BorrowingRecord> findOverdueRecords(@Param("today") LocalDate today);

    // Overdue loans for specific user
    @Query("SELECT r FROM BorrowingRecord r WHERE r.user.id = :userId AND r.status = 'BORROWED' AND r.dueDate < :today ORDER BY r.dueDate ASC")
    List<BorrowingRecord> findOverdueRecordsForUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    long countByStatus(BorrowingStatus status);
}
