package com.parker.rlp.services;

import com.parker.rlp.exceptions.book.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.models.books.BookShelf;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookCaseService {
    void loadBookCases () throws NoSuchBookCaseException;
    List<BookCase> getAllBooks();
    void addBookCase(BookCase bookCase);
    List<String> addBookToBookCase(Book book) throws NoSuchBookCaseException;
    void setBookLocation(int count, Book newBook, BookShelf bookShelf, BookCase bookCase);
}
