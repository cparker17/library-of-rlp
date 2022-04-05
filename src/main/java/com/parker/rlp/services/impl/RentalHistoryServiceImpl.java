package com.parker.rlp.services.impl;

import com.parker.rlp.models.RentalHistory;
import com.parker.rlp.models.User;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.repositories.RentalHistoryRepository;
import com.parker.rlp.services.RentalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RentalHistoryServiceImpl implements RentalHistoryService {
    @Autowired
    RentalHistoryRepository rentalHistoryRepository;

    @Override
    public void updateUserRentalHistory(User user, Book book) {
        RentalHistory rentalHistory = RentalHistory.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .dateRented(book.getDateRented())
                .dateReturned(book.getDateReturned())
                .build();
        user.addRentalHistory(rentalHistory);
        rentalHistoryRepository.save(rentalHistory);
    }
}
