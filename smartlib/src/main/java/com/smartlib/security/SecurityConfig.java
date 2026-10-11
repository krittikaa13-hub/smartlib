package com.smartlib.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration for SmartLib.
 * Enforces role-based authorization:
 * - Public: Homepage, Catalogue, Register, Login, Static assets, Health check
 * - User: User Dashboard, Profile, Circulation
 * - Admin: Admin Dashboard and all /admin/** routes
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Static resources (CSS, JS, images)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Public informative endpoints
                .requestMatchers("/", "/home", "/catalogue", "/catalogue/**", "/syllabus-finder", "/shelf-finder", "/digital-resources").permitAll()
                // Public health endpoint
                .requestMatchers("/api/health").permitAll()
                // Public authentication endpoints
                .requestMatchers("/login", "/register").permitAll()
                // Admin restricted routes
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // User circulation & protected routes
                .requestMatchers("/dashboard", "/my-library", "/my-library/**", "/borrowing-history", "/borrow/**", "/return/**", "/profile/**").authenticated()
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // Custom 403 access denied handling
                .accessDeniedPage("/access-denied")
            )
            // CSRF protection remains enabled by default for all form submissions
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/health"));

        return http.build();
    }
}
