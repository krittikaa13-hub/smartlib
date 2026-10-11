-- ==============================================================================
-- SmartLib Database Initialization Script (Stage 4: Circulation & Borrowing)
-- Database Name: smartlib_db
-- ==============================================================================

-- 1. Create database if it does not already exist with UTF-8 character encoding
CREATE DATABASE IF NOT EXISTS smartlib_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Switch to smartlib_db
USE smartlib_db;

-- 3. Users Table (Stage 2)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Initial Users Seed
INSERT IGNORE INTO users (name, email, password_hash, role, created_at, enabled)
VALUES 
(
    'Library Administrator',
    'admin@smartlib.edu',
    '$2a$10$wK1F5i3g8eY4oO8B5b0i5.1Lq3VnN8vXW7Qj7.V8.yX4H7Jz3eK1W',
    'ADMIN',
    NOW(),
    TRUE
),
(
    'Jane Scholar (Student)',
    'jane.scholar@university.edu',
    '$2a$10$yB1K9s4nQ8v.J3uE6mG2/.5P8hW2qA1cK7mO4pT9rV6sX3yZ8wB4.',
    'USER',
    NOW(),
    TRUE
);

-- 5. Books Table (Stage 3)
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(200),
    publication_year INT,
    category VARCHAR(100) NOT NULL,
    description TEXT,
    shelf_location VARCHAR(100) NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_book_isbn (isbn),
    INDEX idx_book_title (title),
    INDEX idx_book_author (author),
    INDEX idx_book_category (category),
    CONSTRAINT chk_copy_counts CHECK (available_copies >= 0 AND available_copies <= total_copies AND total_copies >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Book Copies Table (Stage 4: Physical Item Tracking)
CREATE TABLE IF NOT EXISTS book_copies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    copy_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    shelf_location VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_copy_code (copy_code),
    INDEX idx_copy_book_status (book_id, status),
    CONSTRAINT fk_book_copy_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT chk_copy_status CHECK (status IN ('AVAILABLE', 'BORROWED', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Borrowing Records Table (Stage 4: Circulation Ledger)
CREATE TABLE IF NOT EXISTS borrowing_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_copy_id BIGINT NOT NULL,
    borrowed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date DATE NOT NULL,
    returned_at DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    INDEX idx_borrow_user_status (user_id, status),
    INDEX idx_borrow_copy_status (book_copy_id, status),
    INDEX idx_borrow_due_date (due_date),
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_copy FOREIGN KEY (book_copy_id) REFERENCES book_copies (id) ON DELETE CASCADE,
    CONSTRAINT chk_borrow_status CHECK (status IN ('BORROWED', 'RETURNED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Verification queries
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_books FROM books;
SELECT COUNT(*) AS total_copies FROM book_copies;
SELECT COUNT(*) AS total_borrowings FROM borrowing_records;
