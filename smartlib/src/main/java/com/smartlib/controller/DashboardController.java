package com.smartlib.controller;

import com.smartlib.entity.User;
import com.smartlib.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * Controller serving authenticated user dashboard and administrative operations console.
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final com.smartlib.service.BorrowingService borrowingService;

    public DashboardController(UserService userService, com.smartlib.service.BorrowingService borrowingService) {
        this.userService = userService;
        this.borrowingService = borrowingService;
    }

    /**
     * Standard student / faculty member dashboard.
     * Displays user's personal account status, circulation summary, due dates, and alerts.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String userDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> userOptional = userService.findByEmail(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return "redirect:/login?error=true";
        }

        User user = userOptional.get();
        model.addAttribute("currentUser", user);
        model.addAttribute("pageTitle", "My Dashboard - SmartLib");
        model.addAttribute("activeNav", "dashboard");

        // Stage 4 circulation metrics & alerts
        var activeLoans = borrowingService.getActiveLoansForUser(user);
        var overdueLoans = borrowingService.getOverdueLoansForUser(user);

        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("activeLoansCount", activeLoans.size());
        model.addAttribute("overdueLoansCount", overdueLoans.size());
        model.addAttribute("maxLoans", borrowingService.getMaxLoans());
        model.addAttribute("reservationsCount", 0);
        model.addAttribute("questPoints", 150); // Starter academic points

        return "user-dashboard";
    }

    /**
     * Protected Administrative Dashboard.
     * Accessible strictly by users possessing the ADMIN role.
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Optional<User> adminOptional = userService.findByEmail(userDetails.getUsername());
        adminOptional.ifPresent(admin -> model.addAttribute("currentAdmin", admin));

        // System overview statistics
        model.addAttribute("totalUsers", userService.getTotalUsersCount());
        model.addAttribute("standardUsers", userService.getStandardUserCount());
        model.addAttribute("adminUsers", userService.getAdminCount());
        model.addAttribute("recentUsers", userService.getRecentUsers());

        // Stage 4 Circulation Statistics
        model.addAttribute("totalCopiesCount", borrowingService.getTotalPhysicalCopiesCount());
        model.addAttribute("totalActiveLoansCount", borrowingService.getTotalActiveLoansAcrossCampus());
        model.addAttribute("totalCompletedReturnsCount", borrowingService.getTotalCompletedReturnsAcrossCampus());

        model.addAttribute("pageTitle", "Administration Console - SmartLib");
        model.addAttribute("activeNav", "admin");

        return "admin/dashboard";
    }
}
