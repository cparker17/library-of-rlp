package com.parker.rlp.services;

import com.parker.rlp.exceptions.user.DuplicateUserException;
import com.parker.rlp.exceptions.user.NoRentalHistoryException;
import com.parker.rlp.exceptions.user.NoSuchUserException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.users.RentalHistory;
import com.parker.rlp.models.users.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<User> getAllUsers();
    User registerAccount(User user) throws DuplicateUserException;
    User getUser(Long id) throws NoSuchUserException;
    void deleteUser(Long id) throws NoSuchUserException;
    List<User> saveAllUsers(List<User> userList);
    void returnBook(Long id, User user);
    void checkoutBook(Book book, User user);
    User updateUser(User user);
    List<RentalHistory> getUserRentalHistory(Long id) throws NoRentalHistoryException, NoSuchUserException;
}
