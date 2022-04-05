package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.DuplicateBookException;
import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.repositories.BookHistoryRepository;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.repositories.UserRepository;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookHistoryService;
import com.parker.rlp.services.BookService;
import com.parker.rlp.services.RentalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    BookCaseService bookCaseService;

    @Autowired
    RentalHistoryService rentalHistoryService;

    @Autowired
    BookHistoryService bookHistoryService;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookHistoryRepository bookHistoryRepository;

    @Override
    public Book saveBook(Book book) throws DuplicateBookException {
        if (bookRepository.findDistinctByIsbn(book.getIsbn()) != null) {
            throw new DuplicateBookException("This book already exists in our system.");
        }
        book.setDateAdded(LocalDate.now());
        return bookRepository.save(book);

    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> saveAllBooks(List<Book> books) {
        return bookRepository.saveAll(books);
    }

    @Override
    public List<Book> getAvailableBooks() throws NoSuchBookException {
        List<Book> availableBooks = bookRepository.findAll();
        availableBooks.removeIf(book -> book.getUser() != null);
        if (availableBooks.isEmpty()) {
            throw new NoSuchBookException("There aren't any books available at this time.");
        }
        return availableBooks;
    }

    @Override
    public void deleteBook(Long id) throws NoSuchBookException, NoSuchBookCaseException {
        if (bookRepository.findById(id).isEmpty()) {
            throw new NoSuchBookException("A book with that id does not exist");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Book getBookByBookId(Long id) throws NoSuchBookException {
        if (bookRepository.findById(id).isEmpty()) {
            throw new NoSuchBookException("A book with that id does not exist.");
        }
        return bookRepository.findById(id).get();
    }

    @Override
    public Book checkoutBook(Long bookId, Long userId) {
        Book book = bookRepository.getById(bookId);
        book.setDateRented(LocalDate.now());
        book.setUser(userRepository.findById(userId).get());
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getUserBooks(Long id) throws NoSuchBookException {
        if (bookRepository.findAllByUserId(id).isEmpty()) {
            throw new NoSuchBookException("Customer does not have any books checked out.");
        }
        return bookRepository.findAllByUserId(id);
    }

    @Override
    public Book returnBook(Long id) {
        Book book = bookRepository.getById(id);
        bookHistoryService.updateBookRentalHistory(book);
        book.setUser(null);
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public List<Book> getNewArrivals() throws NoSuchBookException {
        List<Book> books = bookRepository.findBooksByDateAddedIsAfter(LocalDate.now().minusDays(30L));
        if (books.isEmpty()) {
            throw new NoSuchBookException("We don't have any new books in the last 30 days.");
        }
        return books;
    }

    @Override
    public List<Book> getSearchResults(String searchText) throws NoSuchBookException {
        List<Book> searchResults = new ArrayList<>();
        searchResults.addAll(bookRepository.findBooksBySubjectContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByTitleContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByAuthorContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByIsbnContaining(searchText));
        if (searchResults.isEmpty()) {
            throw new NoSuchBookException("We don't have any books for that search.");
        }
        return searchResults.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public Book getLatestArrival() {
        return bookRepository.findTopByOrderByDateAddedDesc();
    }
}

