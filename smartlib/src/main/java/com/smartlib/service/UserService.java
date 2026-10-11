package com.smartlib.service;

import com.smartlib.dto.RegisterDto;
import com.smartlib.entity.Role;
import com.smartlib.entity.User;
import com.smartlib.exception.DuplicateEmailException;
import com.smartlib.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing user domain operations, registration, password hashing, and user lookups.
 * Isolates business rules from controllers.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new library user.
     * Enforces unique email check, BCrypt password hashing, and default USER role assignment.
     * Rejects any attempt at public ADMIN registration.
     */
    @Transactional
    public User registerUser(RegisterDto registerDto) {
        String normalizedEmail = registerDto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("Registration rejected: email '{}' is already in use", normalizedEmail);
            throw new DuplicateEmailException("An account with email '" + normalizedEmail + "' already exists.");
        }

        // Hash password with BCrypt - plain text password is never stored or logged
        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());

        // Always assign USER role for public registration
        User user = new User(
                registerDto.getName().trim(),
                normalizedEmail,
                encodedPassword,
                Role.USER
        );

        User savedUser = userRepository.save(user);
        log.info("Successfully registered new user: id={}, email={}, role={}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getRecentUsers() {
        return userRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public long getAdminCount() {
        return userRepository.countByRole(Role.ADMIN);
    }

    @Transactional(readOnly = true)
    public long getStandardUserCount() {
        return userRepository.countByRole(Role.USER);
    }
}
