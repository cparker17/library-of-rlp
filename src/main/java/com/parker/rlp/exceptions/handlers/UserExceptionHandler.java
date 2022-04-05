package com.parker.rlp.exceptions.handlers;

import com.parker.rlp.exceptions.user.UserException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserExceptionHandler extends Exception {
    @ExceptionHandler(UserException.class)
    public String handleBookExceptions(UserException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error-page";
    }
}
