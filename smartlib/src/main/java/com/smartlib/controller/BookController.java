package com.smartlib.controller;

import com.smartlib.entity.Book;
import com.smartlib.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controller serving the public book catalogue and detailed book metadata views.
 */
@Controller
public class BookController {

    private final BookService bookService;
    private final com.smartlib.service.BorrowingService borrowingService;

    public BookController(BookService bookService, com.smartlib.service.BorrowingService borrowingService) {
        this.bookService = bookService;
        this.borrowingService = borrowingService;
    }

    /**
     * Public Book Catalogue view with keyword search, category filtering, sorting, and pagination.
     */
    @GetMapping("/catalogue")
    public String viewCatalogue(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "sortBy", required = false, defaultValue = "title") String sortBy,
            @RequestParam(name = "direction", required = false, defaultValue = "asc") String direction,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model) {

        // Validate allowed sort fields to prevent unexpected property errors
        String validSortBy = "author".equalsIgnoreCase(sortBy) ? "author" : "title";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, validSortBy);

        int pageSize = 9;
        int currentPage = Math.max(0, page);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, sort);

        Page<Book> booksPage = bookService.searchCatalogue(query, category, pageRequest);
        List<String> categories = bookService.getAllCategories();

        model.addAttribute("booksPage", booksPage);
        model.addAttribute("query", query != null ? query.trim() : "");
        model.addAttribute("selectedCategory", category != null ? category.trim() : "");
        model.addAttribute("categories", categories);
        model.addAttribute("sortBy", validSortBy);
        model.addAttribute("direction", direction.toLowerCase());
        model.addAttribute("pageTitle", "Book Catalogue - SmartLib");
        model.addAttribute("activeNav", "catalogue");

        // Inventory summary badges
        model.addAttribute("totalActiveTitles", bookService.getActiveTitlesCount());
        model.addAttribute("totalAvailableCopies", bookService.getAvailableCopiesCount());

        return "catalogue";
    }

    /**
     * Book metadata details view displaying full description, shelf locator, and physical copies.
     */
    @GetMapping("/catalogue/{id}")
    public String viewBookDetails(@PathVariable("id") Long id, Model model) {
        Optional<Book> bookOptional = bookService.getActiveBookById(id);

        if (bookOptional.isEmpty()) {
            return "redirect:/catalogue?notFound=true";
        }

        Book book = bookOptional.get();
        model.addAttribute("book", book);
        model.addAttribute("copies", borrowingService.getCopiesForBook(book.getId()));
        model.addAttribute("loanDurationDays", borrowingService.getLoanDurationDays());
        model.addAttribute("maxLoans", borrowingService.getMaxLoans());
        model.addAttribute("pageTitle", book.getTitle() + " - SmartLib Catalogue");
        model.addAttribute("activeNav", "catalogue");

        return "book-details";
    }
}
