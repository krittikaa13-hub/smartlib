package com.smartlib.controller;

import com.smartlib.dto.RegisterDto;
import com.smartlib.exception.DuplicateEmailException;
import com.smartlib.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling user registration, login views, and access-denied notices.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Serves the login page.
     * Redirects already logged-in users directly to their dashboard.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(r -> r.equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/admin/dashboard" : "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid university email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out securely.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! You may now sign in with your credentials.");
        }

        model.addAttribute("pageTitle", "Sign In - SmartLib");
        model.addAttribute("activeNav", "login");
        return "login";
    }

    /**
     * Serves the public user registration page.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", new RegisterDto());
        model.addAttribute("pageTitle", "Create Student / Faculty Account - SmartLib");
        model.addAttribute("activeNav", "register");
        return "register";
    }

    /**
     * Processes public user registration.
     */
    @PostMapping("/register")
    public String registerUserAccount(
            @Valid @ModelAttribute("user") RegisterDto registerDto,
            BindingResult result,
            Model model) {

        // 1. Password confirmation check
        if (!registerDto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match.");
        }

        // 2. Pre-check email uniqueness
        if (!result.hasFieldErrors("email") && userService.findByEmail(registerDto.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "An account with this email address already exists.");
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create Account - SmartLib");
            model.addAttribute("activeNav", "register");
            return "register";
        }

        try {
            userService.registerUser(registerDto);
            return "redirect:/login?registered=true";
        } catch (DuplicateEmailException ex) {
            result.rejectValue("email", "error.user", ex.getMessage());
            model.addAttribute("pageTitle", "Create Account - SmartLib");
            model.addAttribute("activeNav", "register");
            return "register";
        }
    }

    /**
     * Renders 403 Forbidden access-denied view.
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied - SmartLib");
        model.addAttribute("errorTitle", "Access Denied (403 Forbidden)");
        model.addAttribute("errorMessage", "You do not possess the required administrative privileges to access this area.");
        return "access-denied";
    }
}
