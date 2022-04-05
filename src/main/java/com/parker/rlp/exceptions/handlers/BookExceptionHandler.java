package com.parker.rlp.exceptions.handlers;

import com.parker.rlp.exceptions.book.BookException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BookExceptionHandler extends Exception {
    @ExceptionHandler(BookException.class)
    public String handleBookExceptions(BookException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error-page";
    }
}
