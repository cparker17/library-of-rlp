package com.parker.rlp.controllers;

import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.services.BookCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BookCaseController {
    @Autowired
    BookCaseService bookCaseService;

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
}
