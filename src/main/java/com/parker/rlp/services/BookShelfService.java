package com.parker.rlp.services;

import com.parker.rlp.exceptions.book.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.models.books.BookShelf;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookShelfService {
    List<BookShelf> getShelvesWithTargetSubject(Book book);
    BookShelf getTargetBookShelf(List<BookShelf> bookShelvesWithSubject, Book book);
    void addBookToBottomShelf(Book book, BookCase bookCase) throws NoSuchBookCaseException;
    List<String> addBook(Book book, BookShelf targetBookShelf, BookCase bookCase) throws NoSuchBookCaseException;
}
