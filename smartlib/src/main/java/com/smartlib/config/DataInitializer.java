package com.smartlib.config;

import com.smartlib.entity.*;
import com.smartlib.repository.BookCopyRepository;
import com.smartlib.repository.BookRepository;
import com.smartlib.repository.BorrowingRecordRepository;
import com.smartlib.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Secure bootstrap component initializing:
 * 1. Default Administrator account (admin@smartlib.edu)
 * 2. Demonstration Student account (jane.scholar@university.edu)
 * 3. Initial academic catalogue books
 * 4. Physical BookCopy records for each title
 * 5. Initial demonstration loan record to verify circulation features
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:admin@smartlib.edu}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:Admin@123}")
    private String adminPassword;

    @Value("${ADMIN_NAME:Library Administrator}")
    private String adminName;

    public DataInitializer(UserRepository userRepository,
                           BookRepository bookRepository,
                           BookCopyRepository bookCopyRepository,
                           BorrowingRecordRepository borrowingRecordRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRecordRepository = borrowingRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        bootstrapUsers();
        bootstrapDemoCatalogueAndCopies();
    }

    private void bootstrapUsers() {
        // 1. Bootstrap Admin
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            String normalizedEmail = adminEmail.trim().toLowerCase();
            log.info("No Administrator account found. Initializing bootstrap Admin: {}", normalizedEmail);

            String hashedPassword = passwordEncoder.encode(adminPassword);
            User admin = new User(adminName, normalizedEmail, hashedPassword, Role.ADMIN);
            userRepository.save(admin);

            log.info("Initial Administrator account bootstrapped: email='{}', role=ADMIN", normalizedEmail);
        }

        // 2. Bootstrap Demo Student
        String demoStudentEmail = "jane.scholar@university.edu";
        if (!userRepository.existsByEmailIgnoreCase(demoStudentEmail)) {
            User student = new User("Jane Scholar (Student)", demoStudentEmail, passwordEncoder.encode("Password@123"), Role.USER);
            userRepository.save(student);
            log.info("Demo student account bootstrapped: email='{}', role=USER", demoStudentEmail);
        }
    }

    private void bootstrapDemoCatalogueAndCopies() {
        if (bookRepository.count() == 0) {
            log.info("Book catalogue is empty. Seeding initial academic demonstration titles and physical copies...");

            List<Book> demoBooks = List.of(
                new Book(
                    "9780262046305",
                    "Introduction to Algorithms, Fourth Edition",
                    "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein",
                    "MIT Press",
                    2022,
                    "Computer Science",
                    "Comprehensive textbook covering algorithmic design, asymptotic analysis, dynamic programming, and graph algorithms widely referenced in core university curricula.",
                    "Stack 2 - Row B - Bay 1",
                    8,
                    6
                ),
                new Book(
                    "9781119800361",
                    "Operating System Concepts, Tenth Edition",
                    "Abraham Silberschatz, Peter B. Galvin, Greg Gagne",
                    "Wiley",
                    2021,
                    "Computer Science",
                    "The authoritative reference on operating system architectures, process concurrency, virtual memory management, and file systems.",
                    "Stack 2 - Row B - Bay 3",
                    5,
                    5
                ),
                new Book(
                    "9780078022159",
                    "Database System Concepts, Seventh Edition",
                    "Abraham Silberschatz, Henry F. Korth, S. Sudarshan",
                    "McGraw-Hill",
                    2020,
                    "Computer Science",
                    "Foundational text on relational database principles, SQL query optimization, transaction recovery, and distributed database models.",
                    "Stack 2 - Row C - Bay 2",
                    6,
                    4
                ),
                new Book(
                    "9781337613927",
                    "Calculus: Early Transcendentals, Ninth Edition",
                    "James Stewart, Daniel K. Clegg, Saleem Watson",
                    "Cengage Learning",
                    2020,
                    "Mathematics",
                    "Standard undergraduate text on multivariable differential and integral calculus with engineering and physical science applications.",
                    "Stack 1 - Row A - Bay 4",
                    10,
                    9
                ),
                new Book(
                    "9780134494166",
                    "Clean Architecture: A Craftsman's Guide",
                    "Robert C. Martin",
                    "Prentice Hall",
                    2018,
                    "Software Engineering",
                    "Essential software design guide addressing dependency inversion, component coupling, boundary separation, and testability.",
                    "Stack 2 - Row D - Bay 1",
                    4,
                    3
                ),
                new Book(
                    "9780357038314",
                    "Principles of Economics, Ninth Edition",
                    "N. Gregory Mankiw",
                    "Cengage Learning",
                    2021,
                    "Economics",
                    "Foundational undergraduate overview of microeconomic market mechanisms, macroeconomic policy, and fiscal systems.",
                    "Stack 3 - Row A - Bay 2",
                    7,
                    7
                )
            );

            List<Book> savedBooks = bookRepository.saveAll(demoBooks);
            log.info("Successfully seeded {} academic demonstration books.", savedBooks.size());

            // Generate physical BookCopy records for each book
            List<BookCopy> copiesToSave = new ArrayList<>();
            for (Book book : savedBooks) {
                for (int i = 1; i <= book.getTotalCopies(); i++) {
                    String copyCode = String.format("LIB-%03d-C%02d", book.getId(), i);
                    // Set status to match book.availableCopies
                    CopyStatus status = (i <= book.getAvailableCopies()) ? CopyStatus.AVAILABLE : CopyStatus.BORROWED;
                    copiesToSave.add(new BookCopy(book, copyCode, status, book.getShelfLocation()));
                }
            }

            List<BookCopy> savedCopies = bookCopyRepository.saveAll(copiesToSave);
            log.info("Successfully generated {} physical BookCopy inventory records.", savedCopies.size());

            // Seed an active loan and a past returned loan for Jane Scholar
            userRepository.findByEmailIgnoreCase("jane.scholar@university.edu").ifPresent(student -> {
                // Find a borrowed copy from Book 1 (Algorithms)
                savedCopies.stream()
                        .filter(c -> c.getBook().getId().equals(savedBooks.get(0).getId()) && c.getStatus() == CopyStatus.BORROWED)
                        .findFirst()
                        .ifPresent(algoCopy -> {
                            BorrowingRecord activeLoan = new BorrowingRecord(
                                    student,
                                    algoCopy,
                                    LocalDateTime.now().minusDays(3),
                                    LocalDate.now().plusDays(11)
                            );
                            borrowingRecordRepository.save(activeLoan);
                            log.info("Seeded initial active loan: student={}, copyCode={}, due in 11 days",
                                    student.getEmail(), algoCopy.getCopyCode());
                        });

                // Find a borrowed copy from Book 3 (Database Concepts) and mark as completed return
                savedCopies.stream()
                        .filter(c -> c.getBook().getId().equals(savedBooks.get(2).getId()) && c.getStatus() == CopyStatus.AVAILABLE)
                        .findFirst()
                        .ifPresent(dbCopy -> {
                            BorrowingRecord pastLoan = new BorrowingRecord(
                                    student,
                                    dbCopy,
                                    LocalDateTime.now().minusDays(20),
                                    LocalDate.now().minusDays(6)
                            );
                            pastLoan.setStatus(BorrowingStatus.RETURNED);
                            pastLoan.setReturnedAt(LocalDateTime.now().minusDays(7));
                            borrowingRecordRepository.save(pastLoan);
                            log.info("Seeded initial returned loan history record: student={}, copyCode={}",
                                    student.getEmail(), dbCopy.getCopyCode());
                        });
            });
        }
    }
}
