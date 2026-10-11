package com.smartlib.controller;

import com.smartlib.entity.BorrowingRecord;
import com.smartlib.service.BorrowingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

/**
 * Administrative controller for viewing campus-wide borrowing transactions,
 * active loans, overdue accounts, and copy tracking.
 * Strictly restricted to users possessing ROLE_ADMIN.
 */
@Controller
@RequestMapping("/admin/borrowings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBorrowingController {

    private final BorrowingService borrowingService;

    public AdminBorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    /**
     * Lists all borrowing transactions across the university library system.
     */
    @GetMapping
    public String listAllCirculationRecords(Model model) {
        List<BorrowingRecord> allRecords = borrowingService.getAllBorrowingRecordsForAdmin();

        long activeLoans = borrowingService.getTotalActiveLoansAcrossCampus();
        long completedReturns = borrowingService.getTotalCompletedReturnsAcrossCampus();
        long totalCopies = borrowingService.getTotalPhysicalCopiesCount();
        long overdueCount = allRecords.stream().filter(BorrowingRecord::isOverdue).count();

        model.addAttribute("records", allRecords);
        model.addAttribute("activeLoansCount", activeLoans);
        model.addAttribute("completedReturnsCount", completedReturns);
        model.addAttribute("totalCopiesCount", totalCopies);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("pageTitle", "Campus Circulation & Loan Records - Admin");
        model.addAttribute("activeNav", "admin");

        return "admin/borrowings/list";
    }
}
