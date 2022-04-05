package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.User;
import com.parker.rlp.models.UserFactory;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    BookService bookService;

    @Autowired
    UserService userService;

    @GetMapping("/new-arrivals")
    public String viewNewArrivals(Model model) {
        try {
            model.addAttribute("books", bookService.getNewArrivals());
            return "new-arrivals";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @RequestMapping("/available")
    public String viewAvailableBooks(Model model) {
        try {
            model.addAttribute("availableBooksList", bookService.getAvailableBooks());
            return "book-index";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @RequestMapping("/update/{id}")
    public String updateBook(@ModelAttribute(name="book") Book book) {
        bookService.updateBook(book);
        return "redirect:/dashboard";
    }

    @RequestMapping("/delete/{id}")
    public String deleteBook(Model model, @PathVariable(name = "id") Long id) {
        try {
            bookService.deleteBook(id);
            return "redirect:/books";
        } catch (NoSuchBookException | NoSuchBookCaseException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @RequestMapping("/checkout/{bookId}")
    public String checkoutBook(@PathVariable(name = "bookId") Long bookId,
                               Authentication auth, Model model) {
        User user = UserFactory.createUser(auth);
        Book book = bookService.checkoutBook(bookId, user.getId());
        userService.checkoutBook(book, user);
        model.addAttribute("user", user);
        return "user-dashboard";
    }

    @RequestMapping("/return/{bookId}")
    public String returnBook(@PathVariable(name = "bookId") Long bookId,
                             Authentication auth, Model model) {
        bookService.returnBook(bookId);
        User user = UserFactory.createUser(auth);
        userService.returnBook(bookId, user);
        model.addAttribute("user", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/cover-image/{bookId}")
    public String viewBookCoverImage(@PathVariable(name = "bookId") Long bookId, Model model) {
        try {
            model.addAttribute("book", bookService.getBookByBookId(bookId));
            return "cover-image";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @RequestMapping("/books/search")
    public String displaySearchResults(Model model, @RequestParam(value="searchText") String searchText) {
        try {
            model.addAttribute("books", bookService.getSearchResults(searchText));
            return "search-results";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }
}
