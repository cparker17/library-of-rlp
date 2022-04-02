package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.exceptions.NoSuchUserException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
    @Autowired
    UserService userService;

    @Autowired
    BookService bookService;

    @Autowired
    BookCaseService bookCaseService;

    @RequestMapping("/update/{id}")
    public String updateUser(@PathVariable(name = "id") Long id, Model model) {
        try {
            model.addAttribute("user", userService.getUser(id));
            return "update-user";
        } catch (NoSuchUserException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Long id) {
        try {
            userService.deleteUser(id);
            return "redirect:/";
        } catch (NoSuchUserException e) {
            return e.getMessage();
        }
    }

    @GetMapping("/user-list")
    public String viewUserList(Model model) {
        model.addAttribute("userList", userService.getAllUsers());
        return "user-list";
    }

    @GetMapping("/user-books/{id}")
    public String viewUserBooks(@PathVariable(name = "id") Long id, Model model) {
        try {
            model.addAttribute("user", userService.getUser(id));
            return "user-books";
        } catch (NoSuchUserException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @RequestMapping("/books/edit/{id}")
    public String showEditBookPage(Model model, @PathVariable(name = "id") Long id) {
        try {
            Book book = bookService.getBookByBookId(id);
            model.addAttribute("book", book);
            return "edit-book";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/books/rental-history/{id}")
    public String viewBookRentalHistory(Model model, @PathVariable(name = "id") Long id) {
        try {
            model.addAttribute("history", bookService.getBookRentalHistory(id));
            model.addAttribute("book", bookService.getBookByBookId(id));
            return "book-history";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/user/rental-history/{id}")
    public String viewUserRentalHistory(Model model, @PathVariable(name = "id") Long id) {
        try {
            model.addAttribute("history", userService.getUserRentalHistory(id));
            model.addAttribute("user", userService.getUser(id));
            return "user-history";
        } catch(NoSuchUserException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

    }

    @GetMapping("/admin/loadBookCases")
    public String loadBookCases(Model model) {
        try {
            bookCaseService.loadBookCases();
        } catch (NoSuchBookCaseException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
        return "redirect:/";
    }
}
