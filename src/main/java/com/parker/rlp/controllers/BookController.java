package com.parker.rlp.controllers;

import com.parker.rlp.exceptions.book.NoSuchBookCaseException;
import com.parker.rlp.exceptions.book.NoSuchBookException;
import com.parker.rlp.exceptions.user.NoSuchUserException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.users.User;
import com.parker.rlp.models.users.UserFactory;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.BookShelfService;
import com.parker.rlp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    BookShelfService bookShelfService;

    @Autowired
    BookService bookService;

    @Autowired
    UserService userService;

    @GetMapping("/new-arrivals")
    public String viewNewArrivals(Model model) throws NoSuchBookException {
        model.addAttribute("books", bookService.getNewArrivals());
        return "new-arrivals";
    }

    @RequestMapping("/available")
    public String viewAvailableBooks(Model model) throws NoSuchBookException {
        model.addAttribute("availableBooksList", bookService.getAvailableBooks());
        return "book-index";
    }

    @RequestMapping("/rented-books")
    public String viewRentedBooks(Model model) {
        model.addAttribute("rentedBooks", bookService.getAllRentedBooks());
        return "rented-books";
    }

    @RequestMapping("/update/{id}")
    public String updateBook(@ModelAttribute(name="book") Book book) {
        bookService.updateBook(book);
        return "redirect:/dashboard";
    }

    @RequestMapping("/delete/{id}")
    public String deleteBook(Model model, @PathVariable(name = "id") Long id)
            throws NoSuchBookException, NoSuchBookCaseException {
        bookShelfService.removeBookFromShelf(id);
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    @RequestMapping("/checkout/{bookId}")
    public String checkoutBook(@PathVariable(name = "bookId") Long bookId,
                               Authentication auth, Model model) throws NoSuchUserException {
        User user = UserFactory.createUser(auth);
        Book book = bookService.checkoutBook(bookId, user.getId());
        userService.checkoutBook(book, user);
        model.addAttribute("user", user);
        return "user-dashboard";
    }

    @RequestMapping("/return/{bookId}")
    public String returnBook(@PathVariable(name = "bookId") Long bookId,
                             Authentication auth, Model model) throws NoSuchBookException {
        bookService.returnBook(bookId);
        User user = UserFactory.createUser(auth);
        userService.returnBook(bookId, user);
        model.addAttribute("book", bookService.getBookByBookId(bookId));
        return "restock-directions";
    }

    @GetMapping("/cover-image/{bookId}")
    public String viewBookCoverImage(@PathVariable(name = "bookId") Long bookId, Model model)
            throws NoSuchBookException {
        model.addAttribute("book", bookService.getBookByBookId(bookId));
        return "cover-image";
    }

    @RequestMapping("/search")
    public String displaySearchResults(Model model, @RequestParam(value="searchText") String searchText)
            throws NoSuchBookException {
        model.addAttribute("books", bookService.getSearchResults(searchText));
        return "search-results";
    }
}
