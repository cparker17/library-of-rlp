package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.DuplicateUserException;
import com.parker.rlp.exceptions.NoSuchUserException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.RentalHistory;
import com.parker.rlp.models.Role;
import com.parker.rlp.models.User;
import com.parker.rlp.repositories.*;
import com.parker.rlp.services.SecurityUserService;
import com.parker.rlp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    @Autowired
    final UserRepository userRepository;

    @Autowired
    SecurityUserService securityUserService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RentalHistoryRepository rentalHistoryRepository;

    @Autowired
    AddressRepository addressRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User registerAccount(User user) throws DuplicateUserException {
        if (userRepository.findDistinctByEmail(user.getEmail()) != null) {
            throw new DuplicateUserException("This customer already exists in the system.");
        } else {
            Role role = roleRepository.findRoleById(1L);
            user.setRole(role);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        }
    }

    @Override
    public User getUser(Long id) throws NoSuchUserException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new NoSuchUserException("A customer with that id does not exist.");
        } else {
            return optionalUser.get();
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) throws NoSuchUserException {
        if (userRepository.findById(id).isEmpty()) {
            throw new NoSuchUserException("A customer with that id does not exist.");
        } else {
            userRepository.deleteById(id);
        }
    }

    @Transactional
    public List<User> saveAllUsers(List<User> userList) {
        return userRepository.saveAll(userList);
    }

    @Override
    @Transactional
    public void returnBook(Long id, User user) {
        user = userRepository.getById(user.getId());
        updateRentalHistory(user, bookRepository.getById(id));
        List<Book> books = user.getBooks().stream()
                .filter(book -> !book.getId().equals(id))
                .collect(Collectors.toList());
        user.setBooks(books);
        userRepository.save(user);
    }

    private void updateRentalHistory(User user, Book book) {
        RentalHistory rentalHistory = RentalHistory.builder()
                .bookId(book.getId())
                .title(book.getTitle())
                .dateRented(book.getDateRented())
                .dateReturned(book.getDateReturned())
                .build();
        user.addRentalHistory(rentalHistory);
        rentalHistoryRepository.save(rentalHistory);
    }

    @Override
    public void checkoutBook(Book book, User user) {
        user.addBook(book);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User updatedUser) {
        User user = userRepository.getById(updatedUser.getId());
        updatedUser.setUsername(user.getUsername());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setRole(user.getRole());
        resetAddresses(user);
        return userRepository.save(updatedUser);
    }

    private void resetAddresses(User user) {
        Long addressId = user.getAddress().getId();
        addressRepository.deleteById(addressId);
    }

    @Override
    @Transactional
    public List<RentalHistory> getUserRentalHistory(Long id) {
        User user = userRepository.findById(id).get();
        return user.getRentalHistoryList();
    }
}

