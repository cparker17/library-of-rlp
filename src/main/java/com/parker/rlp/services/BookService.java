package com.parker.rlp.services;

import com.parker.rlp.exceptions.DuplicateBookException;
import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.exceptions.NoSuchBookException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.BookHistory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookService {
    Book saveBook(Book book) throws DuplicateBookException;
    List<Book> getAllBooks();
    List<Book> saveAllBooks(List<Book> books);
    List<Book> getAvailableBooks() throws NoSuchBookException;
    void deleteBook(Long id) throws NoSuchBookException, NoSuchBookCaseException;
    Book getBookByBookId(Long id) throws NoSuchBookException;
    Book checkoutBook(Long bookId, Long userId);
    List<Book> getUserBooks(Long id) throws NoSuchBookException;
    Book returnBook(Long id);
    Book updateBook(Book book);
    List<Book> getNewArrivals() throws NoSuchBookException;
    List<Book> getSearchResults(String searchText) throws NoSuchBookException;
    Book getLatestArrival();
}
