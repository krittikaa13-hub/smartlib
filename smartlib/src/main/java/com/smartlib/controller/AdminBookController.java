package com.smartlib.controller;

import com.smartlib.dto.BookDto;
import com.smartlib.entity.Book;
import com.smartlib.exception.DuplicateIsbnException;
import com.smartlib.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Administrative controller for library inventory operations:
 * Adding, editing, viewing, and safely deactivating book titles.
 * Restricted strictly to users possessing the ADMIN role.
 */
@Controller
@RequestMapping("/admin/books")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Lists all books (active and inactive) for administrative management.
     */
    @GetMapping
    public String listAllBooks(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        int pageSize = 15;
        PageRequest pageRequest = PageRequest.of(Math.max(0, page), pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> booksPage = bookService.getAllBooksForAdmin(query, pageRequest);

        model.addAttribute("booksPage", booksPage);
        model.addAttribute("query", query != null ? query.trim() : "");
        model.addAttribute("pageTitle", "Book Inventory Management - Admin");
        model.addAttribute("activeNav", "admin");

        model.addAttribute("totalTitles", bookService.getActiveTitlesCount());
        model.addAttribute("totalCopies", bookService.getTotalCopiesCount());
        model.addAttribute("availableCopies", bookService.getAvailableCopiesCount());

        return "admin/books/list";
    }

    /**
     * Displays form to create a new book record.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        BookDto bookDto = new BookDto();
        bookDto.setTotalCopies(1);
        bookDto.setAvailableCopies(1);
        bookDto.setActive(true);

        model.addAttribute("bookDto", bookDto);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Add New Book - SmartLib Admin");
        model.addAttribute("activeNav", "admin");
        return "admin/books/form";
    }

    /**
     * Handles submission of a new book record.
     */
    @PostMapping
    public String createBook(
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validate copy count logic
        if (!bookDto.isCopyCountValid()) {
            bindingResult.rejectValue("availableCopies", "error.bookDto",
                    "Available copies (" + bookDto.getAvailableCopies() + ") cannot exceed total copies (" + bookDto.getTotalCopies() + ").");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Add New Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        }

        try {
            Book saved = bookService.createBook(bookDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + saved.getTitle() + "' successfully registered in catalogue.");
            return "redirect:/admin/books";
        } catch (DuplicateIsbnException ex) {
            bindingResult.rejectValue("isbn", "error.bookDto", ex.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Add New Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("error.bookDto", ex.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("pageTitle", "Add New Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        }
    }

    /**
     * Displays edit form for an existing book.
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Book> bookOptional = bookService.getBookById(id);

        if (bookOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found with ID: " + id);
            return "redirect:/admin/books";
        }

        Book book = bookOptional.get();
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setCategory(book.getCategory());
        dto.setDescription(book.getDescription());
        dto.setShelfLocation(book.getShelfLocation());
        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());
        dto.setActive(book.isActive());

        model.addAttribute("bookDto", dto);
        model.addAttribute("bookId", book.getId());
        model.addAttribute("isEdit", true);
        model.addAttribute("pageTitle", "Edit Book: " + book.getTitle());
        model.addAttribute("activeNav", "admin");
        return "admin/books/form";
    }

    /**
     * Processes updating an existing book.
     */
    @PostMapping("/{id}/edit")
    public String updateBook(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!bookDto.isCopyCountValid()) {
            bindingResult.rejectValue("availableCopies", "error.bookDto",
                    "Available copies (" + bookDto.getAvailableCopies() + ") cannot exceed total copies (" + bookDto.getTotalCopies() + ").");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Edit Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        }

        try {
            Book updated = bookService.updateBook(id, bookDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + updated.getTitle() + "' successfully updated.");
            return "redirect:/admin/books";
        } catch (DuplicateIsbnException ex) {
            bindingResult.rejectValue("isbn", "error.bookDto", ex.getMessage());
            model.addAttribute("bookId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Edit Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("error.bookDto", ex.getMessage());
            model.addAttribute("bookId", id);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Edit Book - SmartLib Admin");
            model.addAttribute("activeNav", "admin");
            return "admin/books/form";
        }
    }

    /**
     * Safely toggles active status (deactivates / reactivates) without deleting historical records.
     */
    @PostMapping("/{id}/deactivate")
    public String toggleDeactivate(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Book toggled = bookService.toggleBookActiveStatus(id);
            String statusWord = toggled.isActive() ? "reactivated and visible in catalogue" : "deactivated and hidden from public search";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book '" + toggled.getTitle() + "' is now " + statusWord + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating book status: " + ex.getMessage());
        }
        return "redirect:/admin/books";
    }
}
