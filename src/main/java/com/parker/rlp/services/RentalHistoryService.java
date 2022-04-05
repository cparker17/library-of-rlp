package com.parker.rlp.services;

import com.parker.rlp.models.users.User;
import com.parker.rlp.models.books.Book;
import org.springframework.stereotype.Service;

@Service
public interface RentalHistoryService {
    void updateUserRentalHistory(User user, Book book);
}
