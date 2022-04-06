package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.book.DuplicateBookException;
import com.parker.rlp.exceptions.book.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
public class BookCaseController {
    @Autowired
    BookCaseService bookCaseService;

    @Autowired
    BookService bookService;

    @GetMapping("/admin/loadBookCases")
    public String loadBookCases(Model model) throws NoSuchBookCaseException {
        bookCaseService.loadBookCases();
        return "book-index";
    }

    @RequestMapping("/bookcase/new")
    public String displayBookCaseOperations(Model model) {
        model.addAttribute("bookcase", new BookCase());
        return "new-bookcase";
    }

    @RequestMapping("/bookcase/save")
    public String addBookCase(@ModelAttribute BookCase bookCase) {
        bookCaseService.addBookCase(bookCase);
        return "book-index";
    }

    @RequestMapping("/books/save")
    public String saveBookToBookCase(Model model, @Valid @ModelAttribute("book") Book book, Errors errors)
            throws DuplicateBookException, NoSuchBookCaseException {
        if (errors.hasErrors()) {
            return "new-book";
        }
        bookService.saveBook(book);
        model.addAttribute("shiftDirections", bookCaseService.addBookToBookCase(book));
        model.addAttribute("book", book);
        return "book-location";
    }

    @GetMapping("/books")
    public String viewBookCases(Model model) {
        model.addAttribute("bookCases", bookCaseService.getAllBooks());
        return "book-index";
    }
}
