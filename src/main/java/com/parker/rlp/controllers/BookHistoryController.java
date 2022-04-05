package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.services.BookHistoryService;
import com.parker.rlp.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookHistoryController {
    @Autowired
    BookHistoryService bookHistoryService;

    @Autowired
    BookService bookService;

    @GetMapping("/books/rental-history/{id}")
    public String viewBookRentalHistory(Model model, @PathVariable(name = "id") Long id) {
        try {
            model.addAttribute("history", bookHistoryService.getBookRentalHistory(id));
            model.addAttribute("book", bookService.getBookByBookId(id));
            return "book-history";
        } catch (NoSuchBookException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }
}
