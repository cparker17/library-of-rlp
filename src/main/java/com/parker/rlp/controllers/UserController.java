package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.user.NoSuchUserException;
import com.parker.rlp.models.users.User;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user) {
        userService.updateUser(user);
        return "redirect:/dashboard";
    }

    @GetMapping("/user-list")
    public String viewUserList(Model model) {
        model.addAttribute("userList", userService.getAllUsers());
        return "user-list";
    }

    @GetMapping("/user-books/{id}")
    public String viewUserBooks(@PathVariable(name = "id") Long id, Model model) throws NoSuchUserException {
        model.addAttribute("user", userService.getUser(id));
        return "user-books";
    }

    @RequestMapping("/update/{id}")
    public String updateUser(@PathVariable(name = "id") Long id, Model model) throws NoSuchUserException {
        model.addAttribute("user", userService.getUser(id));
        return "update-user";
    }

    @RequestMapping("/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Long id) throws NoSuchUserException {
        userService.deleteUser(id);
        return "redirect:/";
    }

}
