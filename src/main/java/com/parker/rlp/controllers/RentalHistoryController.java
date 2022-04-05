package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.NoRentalHistoryException;
import com.parker.rlp.exceptions.NoSuchUserException;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RentalHistoryController {
    @Autowired
    UserService userService;

    @GetMapping("/user/rental-history/{id}")
    public String viewUserRentalHistory(Model model, @PathVariable(name = "id") Long id) {
        try {
            model.addAttribute("history", userService.getUserRentalHistory(id));
            model.addAttribute("user", userService.getUser(id));
            return "user-history";
        } catch(NoSuchUserException | NoRentalHistoryException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }

    }
}
