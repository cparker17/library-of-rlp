package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.DuplicateBookException;
import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.BookHistory;
import com.parker.rlp.repositories.BookHistoryRepository;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.repositories.UserRepository;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookHistoryRepository bookHistoryRepository;

    @Autowired
    BookCaseService bookCaseService;

    public BookServiceImpl(BookRepository bookRepository, UserRepository userRepository,
                           BookHistoryRepository rentalHistoryRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookHistoryRepository = rentalHistoryRepository;
    }

    @Override
    public Book saveBook(Book book) throws DuplicateBookException {
        if (bookRepository.findDistinctByIsbn(book.getIsbn()) != null) {
            throw new DuplicateBookException("This book already exists in our system.");
        } else {
            return bookRepository.save(book);
        }
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
        } else {
            return availableBooks;
        }
    }

    @Override
    public void deleteBook(Long id) throws NoSuchBookException, NoSuchBookCaseException {
        if (bookRepository.findById(id).isEmpty()) {
            throw new NoSuchBookException("A book with that id does not exist");
        } else {
            bookRepository.deleteById(id);
            //bookCaseService.loadBookCases();
        }
    }

    @Override
    public Book getBookByBookId(Long id) throws NoSuchBookException {
        if (bookRepository.findById(id).isEmpty()) {
            throw new NoSuchBookException("A book with that id does not exist.");
        } else {
            return bookRepository.findById(id).get();
        }
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
        } else {
            return bookRepository.findAllByUserId(id);
        }
    }

    @Override
    public Book returnBook(Long id) {
        Book book = bookRepository.getById(id);
        book.setDateReturned(LocalDate.now());
        BookHistory rentalHistory = BookHistory.builder()
                .username(book.getUser().getUsername())
                .dateRented(book.getDateRented())
                .dateReturned(book.getDateReturned())
                .build();
        book.addRentalHistory(rentalHistory);
        bookHistoryRepository.save(rentalHistory);
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
        } else {
            return books;
        }
    }

    @Override
    public List<Book> getSearchResults(String searchText) throws NoSuchBookException {
        List<Book> searchResults = new ArrayList<>();
        searchResults.addAll(bookRepository.findBooksBySubjectContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByTitleContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByAuthorContaining(searchText));
        searchResults.addAll(bookRepository.findBooksByIsbnContaining(searchText));
        searchResults = searchResults.stream()
                .distinct()
                .collect(Collectors.toList());
        return searchResults;
    }

    @Override
    public List<BookHistory> getBookRentalHistory(Long id) {
        Book book = bookRepository.findById(id).get();
        return book.getBookHistoryList();
    }

    @Override
    public Book getLatestArrival() {
        return bookRepository.findTopByOrderByDateAddedDesc();
    }
}

