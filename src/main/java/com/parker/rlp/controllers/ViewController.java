package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.book.NoSuchBookException;
import com.parker.rlp.exceptions.user.NoSuchUserException;
import com.parker.rlp.models.users.SecurityUser;
import com.parker.rlp.models.users.User;
import com.parker.rlp.models.users.UserFactory;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.repositories.SubjectRepository;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.SecurityUserService;
import com.parker.rlp.services.SubjectService;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
    @Autowired
    SecurityUserService securityUserService;

    @Autowired
    UserService userService;

    @Autowired
    BookService bookService;

    @Autowired
    SubjectService subjectService;

    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("book", bookService.getLatestArrival());
        return "home";
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/dashboard")
    public String viewDashboard(Model model, Authentication auth) throws NoSuchUserException {
        User user = UserFactory.createUser(auth);
        user = userService.getUser(user.getId());
        model.addAttribute("user", user);
        switch (user.getRole().getRole()) {
            case ROLE_USER:
                return "user-dashboard";
            case ROLE_ADMIN:
                return "admin-dashboard";
        }
        model.addAttribute("message", "System error.  Try again.");
        return "error-page";
    }

    @RequestMapping("/books/edit/{id}")
    public String viewEditBookPage(Model model, @PathVariable(name = "id") Long id) throws NoSuchBookException {
        model.addAttribute("book", bookService.getBookByBookId(id));
        return "edit-book";
    }

    @GetMapping("/sign-in")
    public String viewSignInPage(Model model) {
        model.addAttribute("securityUser", new SecurityUser());
        return "login";
    }

    @GetMapping("/books/new")
    public String viewNewBookPage(Model model) {
        model.addAttribute("subjects", subjectService.getAllSubjects());
        model.addAttribute("book", new Book());
        return "new-book";
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
}
