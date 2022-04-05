package com.parker.rlp.services.impl;

import com.parker.rlp.models.books.BookHistory;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.repositories.BookHistoryRepository;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.services.BookHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookHistoryServiceImpl implements BookHistoryService {
    @Autowired
    BookHistoryRepository bookHistoryRepository;

    @Autowired
    BookRepository bookRepository;

    @Override
    public List<BookHistory> getBookRentalHistory(Long id) {
        Optional<Book> bookOptional = bookRepository.findById(id);
        return bookOptional.map(Book::getBookHistoryList).orElse(null);
    }

    @Override
    public void updateBookRentalHistory(Book book) {
        book.setDateReturned(LocalDate.now());
        BookHistory bookHistory = BookHistory.builder()
                .username(book.getUser().getUsername())
                .dateRented(book.getDateRented())
                .dateReturned(book.getDateReturned())
                .build();
        book.addRentalHistory(bookHistory);
        bookHistoryRepository.save(bookHistory);
    }
}
