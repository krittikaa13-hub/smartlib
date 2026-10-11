package com.smartlib;

import com.smartlib.dto.RegisterDto;
import com.smartlib.entity.Role;
import com.smartlib.entity.User;
import com.smartlib.exception.DuplicateEmailException;
import com.smartlib.repository.UserRepository;
import com.smartlib.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot Security and Authentication Test Suite (Stage 2).
 * Verifies user registration, duplicate email rejection, BCrypt hashing,
 * form authentication, role-based access control (RBAC), and session logout.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmartLibSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // 1. Successful User Registration
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("1. Successful user registration persists user with USER role")
    void testSuccessfulUserRegistration() {
        RegisterDto dto = new RegisterDto(
                "Jane Scholar",
                "jane.scholar@university.edu",
                "Password@123",
                "Password@123"
        );

        User registered = userService.registerUser(dto);

        assertNotNull(registered.getId());
        assertEquals("Jane Scholar", registered.getName());
        assertEquals("jane.scholar@university.edu", registered.getEmail());
        assertEquals(Role.USER, registered.getRole());
        assertTrue(registered.isEnabled());
    }

    // -------------------------------------------------------------------------
    // 2. Duplicate Email Rejection
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("2. Registration rejects duplicate email addresses with DuplicateEmailException")
    void testDuplicateEmailRejection() {
        RegisterDto firstDto = new RegisterDto(
                "First Student",
                "duplicate.check@university.edu",
                "Pass12345",
                "Pass12345"
        );
        userService.registerUser(firstDto);

        RegisterDto duplicateDto = new RegisterDto(
                "Second Student",
                "duplicate.check@university.edu",
                "DifferentPass",
                "DifferentPass"
        );

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(duplicateDto));
    }

    // -------------------------------------------------------------------------
    // 3. Password Hashing
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("3. Passwords must be hashed with BCrypt; raw password must not match stored hash")
    void testPasswordHashing() {
        String rawPassword = "SecurePass#2026";
        RegisterDto dto = new RegisterDto(
                "Alice Research",
                "alice.research@university.edu",
                rawPassword,
                rawPassword
        );

        User user = userService.registerUser(dto);

        // Raw password must NEVER equal stored hash
        assertNotEquals(rawPassword, user.getPasswordHash());
        // Stored hash must start with standard BCrypt identifier
        assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
        // Matches verification
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
        assertFalse(passwordEncoder.matches("WrongPassword", user.getPasswordHash()));
    }

    // -------------------------------------------------------------------------
    // 4. Successful Login
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("4. User can authenticate with valid credentials via form login")
    void testSuccessfulLogin() throws Exception {
        RegisterDto dto = new RegisterDto(
                "Bob Reader",
                "bob.reader@university.edu",
                "ReaderPass#123",
                "ReaderPass#123"
        );
        userService.registerUser(dto);

        mockMvc.perform(post("/login")
                .param("email", "bob.reader@university.edu")
                .param("password", "ReaderPass#123")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    // -------------------------------------------------------------------------
    // 5. Invalid Login Rejection
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("5. Login with invalid password redirects to /login?error=true")
    void testInvalidLoginRejection() throws Exception {
        mockMvc.perform(post("/login")
                .param("email", "nonexistent@university.edu")
                .param("password", "WrongPassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // -------------------------------------------------------------------------
    // 6. USER denied access to /admin/**
    // -------------------------------------------------------------------------
    @Test
    @WithMockUser(username = "student@university.edu", roles = {"USER"})
    @DisplayName("6. Authenticated USER is denied access to /admin/** with 403 Forbidden")
    void testUserDeniedAccessToAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // 7. ADMIN allowed access to /admin/**
    // -------------------------------------------------------------------------
    @Test
    @WithMockUser(username = "admin@smartlib.edu", roles = {"ADMIN"})
    @DisplayName("7. Authenticated ADMIN is granted access to /admin/** with HTTP 200")
    void testAdminAllowedAccessToAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("totalUsers"));
    }

    // -------------------------------------------------------------------------
    // 8. Unauthenticated access redirected to login
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("8. Anonymous user accessing protected /dashboard is redirected to /login")
    void testUnauthenticatedAccessRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // -------------------------------------------------------------------------
    // 9. Logout and Session Invalidation
    // -------------------------------------------------------------------------
    @Test
    @WithMockUser(username = "active.user@university.edu", roles = {"USER"})
    @DisplayName("9. POST /logout invalidates session and redirects to /login?logout=true")
    void testLogoutAndSessionBehavior() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }
}
