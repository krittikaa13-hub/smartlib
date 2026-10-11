# SmartLib: Unified University Library Hub

**Stage 4: Borrowing, Returns, Physical Copy Tracking & Transaction History**

SmartLib is a modern university library web application engineered with **Java 21**, **Spring Boot 3.3.4**, **Spring Security 6**, **Thymeleaf**, and **MySQL**. It provides unified book discovery, physical inventory custody tracking, borrowing policies, due date notifications, and an immutable circulation ledger.

Stage 4 introduces physical `BookCopy` item tracking, transactional checkouts, due date calculation, user borrowing limits, self-service returns, personal loan management (`/my-library`), transaction history (`/borrowing-history`), and an administrative circulation audit console (`/admin/borrowings`).

---

## 1. Technology Stack (Stage 4)

- **Language:** Java 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Security:** Spring Security 6 (RBAC, CSRF protection, URL authorization, UserDetails)
- **Web Layer:** Spring MVC & Thymeleaf Template Engine + Thymeleaf Spring Security 6 dialect
- **Persistence:** Spring Data JPA / Hibernate 6 (`Book`, `BookCopy`, `BorrowingRecord`, `User`)
- **Database:** MySQL 8.x (`smartlib_db`)
- **Validation:** Spring Boot Starter Validation (Hibernate Validator / Jakarta Validation)
- **Build Tool:** Apache Maven 3.9+
- **Styling:** Semantic HTML5, Vanilla CSS3 (Deep Navy `#0f172a` & Royal Blue `#2563eb` theme)

---

## 2. Project Directory Structure

```text
smartlib/
├── pom.xml
├── README.md
├── .env.example
├── schema.sql
└── src/
    ├── main/
    │   ├── java/com/smartlib/
    │   │   ├── SmartLibApplication.java
    │   │   ├── config/
    │   │   │   ├── WebMvcConfig.java
    │   │   │   └── DataInitializer.java        # Admin, demo student, copies & loan seeding
    │   │   ├── controller/
    │   │   │   ├── HomeController.java         # Public portal routes
    │   │   │   ├── AuthController.java         # /login, /register, /access-denied
    │   │   │   ├── DashboardController.java    # /dashboard (USER), /admin/dashboard (ADMIN)
    │   │   │   ├── BookController.java         # /catalogue, /catalogue/{id} (Public)
    │   │   │   ├── AdminBookController.java    # /admin/books/** (Admin only)
    │   │   │   ├── BorrowingController.java    # /my-library, /borrow/{id}, /return/{id}
    │   │   │   ├── AdminBorrowingController.java # /admin/borrowings (Admin audit)
    │   │   │   └── HealthController.java       # Health API
    │   │   ├── dto/
    │   │   │   ├── RegisterDto.java            # User registration DTO
    │   │   │   └── BookDto.java                # Book create/edit form DTO
    │   │   ├── entity/
    │   │   │   ├── User.java                   # User entity with unique email index
    │   │   │   ├── Role.java                   # Enum: USER, ADMIN
    │   │   │   ├── Book.java                   # Book entity with copy invariants
    │   │   │   ├── BookCopy.java               # Physical copy barcoded item
    │   │   │   ├── CopyStatus.java             # Enum: AVAILABLE, BORROWED, INACTIVE
    │   │   │   ├── BorrowingRecord.java        # Circulation transaction ledger
    │   │   │   └── BorrowingStatus.java        # Enum: BORROWED, RETURNED
    │   │   ├── exception/
    │   │   │   ├── DuplicateEmailException.java
    │   │   │   ├── DuplicateIsbnException.java
    │   │   │   ├── NoAvailableCopyException.java
    │   │   │   ├── BorrowingLimitExceededException.java
    │   │   │   ├── UnauthorizedActionException.java
    │   │   │   ├── InvalidReturnException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── BookRepository.java
    │   │   │   ├── BookCopyRepository.java     # Copy lookups & status counts
    │   │   │   └── BorrowingRecordRepository.java # Active loans & transaction history
    │   │   ├── security/
    │   │   │   ├── SecurityConfig.java         # Route authorization & authentication
    │   │   │   └── CustomAuthenticationSuccessHandler.java
    │   │   └── service/
    │   │       ├── UserService.java
    │   │       ├── CustomUserDetailsService.java
    │   │       ├── BookService.java
    │   │       └── BorrowingService.java       # Checkout, returns & consistency
    │   └── resources/
    │       ├── templates/
    │       │   ├── home.html                   # Live portal homepage
    │       │   ├── catalogue.html              # Searchable catalogue with filters
    │       │   ├── book-details.html           # Full metadata, copies & borrow button
    │       │   ├── my-library.html             # Active loans, due dates & return actions
    │       │   ├── borrowing-history.html      # Chronological circulation ledger
    │       │   ├── login.html                  # Spring Security sign-in
    │       │   ├── register.html               # Registration form
    │       │   ├── user-dashboard.html         # Student/Faculty dashboard with loan alerts
    │       │   ├── access-denied.html          # HTTP 403 Forbidden page
    │       │   ├── placeholder.html
    │       │   ├── error.html
    │       │   ├── admin/
    │       │   │   ├── dashboard.html          # Admin console
    │       │   │   ├── books/
    │       │   │   │   ├── list.html           # Inventory table & actions
    │       │   │   │   └── form.html           # Add / Edit book form
    │       │   │   └── borrowings/
    │       │   │       └── list.html           # Campus circulation audit ledger
    │       │   └── fragments/
    │       │       ├── header.html             # Navigation with dynamic auth links
    │       │       └── footer.html
    │       ├── static/
    │       │   ├── css/style.css               # Responsive design & circulation styles
    │       │   └── js/app.js
    │       └── application.properties
    └── test/
        └── java/com/smartlib/
            ├── SmartLibApplicationTests.java
            ├── SmartLibSecurityTests.java      # 9 security & RBAC tests
            ├── SmartLibBookTests.java          # 9 catalogue & inventory tests
            └── SmartLibBorrowingTests.java     # 11 circulation & return tests
```

---

## 3. Circulation & Borrowing Policy Defaults

Configured in `application.properties`:
- `smartlib.borrowing.max-loans=3` (Maximum concurrent active physical checkouts per student)
- `smartlib.borrowing.loan-duration-days=14` (Default checkout period from borrow timestamp)

*Note: These are documented prototype defaults. Alliance University official circulation policy will be integrated upon official policy publication.*

---

## 4. Complete Circulation Route Map

| Route | Method | Access | Description |
|---|---|---|---|
| `/my-library` | `GET` | **USER/ADMIN** | View active loans, due dates, overdue notices, and return buttons |
| `/borrowing-history` | `GET` | **USER/ADMIN** | Chronological ledger of all past and present borrow transactions |
| `/borrow/{bookId}` | `POST` | **USER/ADMIN** | Borrow an available physical copy of the title (atomic claim) |
| `/return/{recordId}` | `POST` | **USER/ADMIN** | Return a borrowed physical copy (ownership enforced server-side) |
| `/admin/borrowings` | `GET` | **ADMIN** | View campus-wide circulation audit ledger and overdue items |
| `/catalogue/{id}` | `GET` | Public | View book details, physical barcodes list, and borrow button |

---

## 5. Running the Application & Automated Tests

```bash
cd smartlib

# 1. Compile and run Spring Boot:
mvn clean compile
mvn spring-boot:run

# 2. Run all Stage 4 Circulation tests:
mvn test -Dtest=SmartLibBorrowingTests

# 3. Run entire test suite (all 29 tests across stages):
mvn test
```

---

## 6. Stage 4 Automated Test Suite (`SmartLibBorrowingTests.java`)

1. `testSuccessfulBorrowing()` — Verifies successful checkout of an available copy and copy status transition to `BORROWED`.
2. `testRejectionWhenNoCopiesAvailable()` — Throws `NoAvailableCopyException` when all copies are checked out.
3. `testPreventDuplicateActiveLoanOfSameTitle()` — Prevents checking out multiple copies of the exact same title concurrently.
4. `testSuccessfulReturnByBorrower()` — Borrower returns copy; status transitions to `RETURNED` and copy returns to `AVAILABLE`.
5. `testRejectionOfReturnByDifferentUser()` — Throws `UnauthorizedActionException` when another user tries to return a loan.
6. `testRejectionOfRepeatedReturns()` — Throws `InvalidReturnException` when attempting to return an already returned loan.
7. `testCorrectDueDateCalculation()` — Asserts due date is exactly `today + loanDurationDays` (14 days).
8. `testBorrowingLimitEnforcement()` — Throws `BorrowingLimitExceededException` when attempting checkout beyond max allowed (3).
9. `testPreservationOfTransactionHistory()` — Asserts completed records remain permanently in the database for audit integrity.
10. `testAvailabilityConsistency()` — Asserts `BookCopy` availability count and `Book.availableCopies` remain strictly synchronized.
11. `testAccessControlForCirculationRoutes()` — Asserts anonymous users redirect to `/login` and non-admins receive HTTP 403 on `/admin/borrowings`.

---

## 7. Known Limitations & Roadmap

- **Syllabus Matching:** Linking course syllabi to required course readings belongs to **Prompt 5**.
- **Physical Shelf Maps:** Visual floor plan wayfinding coordinates belong to **Prompt 6**.
- **Deadline Email / SMS Notifications:** Integration with automated mail delivery belongs to **Prompt 6**.
- **Learning Rewards & Quests:** Reading achievements and leaderboard belong to **Prompt 7**.
