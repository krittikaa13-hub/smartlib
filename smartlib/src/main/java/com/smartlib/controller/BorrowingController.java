package com.smartlib.controller;

import com.smartlib.entity.BorrowingRecord;
import com.smartlib.entity.User;
import com.smartlib.exception.BorrowingLimitExceededException;
import com.smartlib.exception.InvalidReturnException;
import com.smartlib.exception.NoAvailableCopyException;
import com.smartlib.exception.UnauthorizedActionException;
import com.smartlib.repository.UserRepository;
import com.smartlib.service.BorrowingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller handling user-facing borrowing, return actions,
 * active loans dashboard, and borrowing transaction history.
 */
@Controller
public class BorrowingController {

    private static final Logger log = LoggerFactory.getLogger(BorrowingController.class);

    private final BorrowingService borrowingService;
    private final UserRepository userRepository;

    public BorrowingController(BorrowingService borrowingService, UserRepository userRepository) {
        this.borrowingService = borrowingService;
        this.userRepository = userRepository;
    }

    /**
     * User's My Library page: displays active physical loans, due dates, and return actions.
     */
    @GetMapping("/my-library")
    public String viewMyLibrary(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveAuthenticatedUser(userDetails);

        List<BorrowingRecord> activeLoans = borrowingService.getActiveLoansForUser(user);
        List<BorrowingRecord> overdueLoans = borrowingService.getOverdueLoansForUser(user);

        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("overdueLoans", overdueLoans);
        model.addAttribute("maxLoans", borrowingService.getMaxLoans());
        model.addAttribute("activeLoansCount", activeLoans.size());
        model.addAttribute("loanDurationDays", borrowingService.getLoanDurationDays());
        model.addAttribute("pageTitle", "My Library & Active Loans - SmartLib");
        model.addAttribute("activeNav", "my-library");

        return "my-library";
    }

    /**
     * Complete borrowing transaction history (both current and returned loans).
     */
    @GetMapping("/borrowing-history")
    public String viewBorrowingHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveAuthenticatedUser(userDetails);

        List<BorrowingRecord> historyRecords = borrowingService.getBorrowingHistoryForUser(user);

        model.addAttribute("historyRecords", historyRecords);
        model.addAttribute("pageTitle", "Borrowing Transaction History - SmartLib");
        model.addAttribute("activeNav", "my-library");

        return "borrowing-history";
    }

    /**
     * Processes borrowing an available physical copy of a book title.
     */
    @PostMapping("/borrow/{bookId}")
    public String borrowBook(
            @PathVariable("bookId") Long bookId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = resolveAuthenticatedUser(userDetails);

        try {
            BorrowingRecord record = borrowingService.borrowBook(user, bookId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Success! You checked out copy [" + record.getBookCopy().getCopyCode() +
                    "] of '" + record.getBookCopy().getBook().getTitle() +
                    "'. Return due date: " + record.getDueDate() + ".");
            return "redirect:/my-library";
        } catch (BorrowingLimitExceededException | NoAvailableCopyException | IllegalStateException | IllegalArgumentException ex) {
            log.warn("Borrowing failed: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/catalogue/" + bookId;
        }
    }

    /**
     * Processes returning a currently borrowed physical copy.
     */
    @PostMapping("/return/{recordId}")
    public String returnBook(
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = resolveAuthenticatedUser(userDetails);

        try {
            BorrowingRecord record = borrowingService.returnBook(user, recordId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Success! Returned copy [" + record.getBookCopy().getCopyCode() +
                    "] of '" + record.getBookCopy().getBook().getTitle() +
                    "'. Physical inventory updated.");
        } catch (UnauthorizedActionException ex) {
            log.warn("Unauthorized return attempt: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (InvalidReturnException ex) {
            log.warn("Invalid return attempt: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/my-library";
    }

    private User resolveAuthenticatedUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("User must be authenticated to perform circulation actions.");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database: " + userDetails.getUsername()));
    }
}
