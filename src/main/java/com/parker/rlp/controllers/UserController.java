package com.parker.rlp.controllers;

import com.parker.rlp.models.User;
import com.parker.rlp.models.UserFactory;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
    private final UserService userService;
    private final BookService bookService;

    public UserController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/register-form")
    public String viewRegisterAccountPage(Model model) {
        model.addAttribute(new User());
        return "register";
    }

    @GetMapping("/update")
    public String viewUpdateUserPage(Authentication auth, Model model) {
        model.addAttribute("user", UserFactory.createUser(auth));
        return "update-user";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user) {
        userService.updateUser(user);
        return "redirect:/dashboard";
    }
}
