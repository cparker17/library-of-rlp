package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.DuplicateUserException;
import com.parker.rlp.models.SecurityUser;
import com.parker.rlp.models.User;
import com.parker.rlp.services.SecurityUserService;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class SecurityUserController {
    @Autowired
    UserService userService;

    @Autowired
    SecurityUserService securityUserService;

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @PostMapping("/register")
    public String registerAccount(@Valid @ModelAttribute(name="user") User user, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return "register";
        }
        try {
            userService.registerAccount(user);
            return "register-success";
        } catch (DuplicateUserException e) {
            model.addAttribute("message", e.getMessage());
            return "error-page";
        }
    }

    @GetMapping("/sign-in")
    public String viewSignInPage(Model model) {
        model.addAttribute("securityUser", new SecurityUser());
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("message", "Invalid username and password.");
        return "login-error";
    }
}
