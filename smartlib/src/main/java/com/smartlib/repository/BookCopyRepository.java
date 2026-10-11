package com.smartlib.repository;

import com.smartlib.entity.BookCopy;
import com.smartlib.entity.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for physical BookCopy records.
 */
@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    List<BookCopy> findByBookId(Long bookId);

    List<BookCopy> findByBookIdAndStatus(Long bookId, CopyStatus status);

    // Atomically find the first available copy for checkout
    @Query("SELECT c FROM BookCopy c WHERE c.book.id = :bookId AND c.status = :status AND c.book.active = true ORDER BY c.id ASC")
    List<BookCopy> findAvailableCopiesForBook(@Param("bookId") Long bookId, @Param("status") CopyStatus status);

    Optional<BookCopy> findByCopyCode(String copyCode);

    boolean existsByCopyCode(String copyCode);

    long countByBookIdAndStatus(Long bookId, CopyStatus status);

    long countByStatus(CopyStatus status);
}
