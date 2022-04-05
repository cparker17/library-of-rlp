package com.parker.rlp.services;

import com.parker.rlp.models.books.BookHistory;
import com.parker.rlp.models.books.Book;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookHistoryService {
    List<BookHistory> getBookRentalHistory(Long id);
    void updateBookRentalHistory(Book book);
}
