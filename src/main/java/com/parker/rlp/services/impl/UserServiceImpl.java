package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.user.DuplicateUserException;
import com.parker.rlp.exceptions.user.NoRentalHistoryException;
import com.parker.rlp.exceptions.user.NoSuchUserException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.users.RentalHistory;
import com.parker.rlp.models.users.Role;
import com.parker.rlp.models.users.User;
import com.parker.rlp.repositories.*;
import com.parker.rlp.services.AddressService;
import com.parker.rlp.services.RentalHistoryService;
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
    SecurityUserService securityUserService;

    @Autowired
    RentalHistoryService rentalHistoryService;

    @Autowired
    AddressService addressService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User registerAccount(User user) throws DuplicateUserException {
        if (userRepository.findDistinctByEmail(user.getEmail()) != null) {
            throw new DuplicateUserException("This customer already exists in the system.");
        }
        Role role = roleRepository.findRoleById(1L);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getUser(Long id) throws NoSuchUserException {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new NoSuchUserException("A customer with that id does not exist.");
        }
        return optionalUser.get();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) throws NoSuchUserException {
        if (userRepository.findById(id).isEmpty()) {
            throw new NoSuchUserException("A customer with that id does not exist.");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public List<User> saveAllUsers(List<User> userList) {
        return userRepository.saveAll(userList);
    }

    @Override
    @Transactional
    public void returnBook(Long id, User user) {
        user = userRepository.getById(user.getId());
        rentalHistoryService.updateUserRentalHistory(user, bookRepository.getById(id));
        user.setBooks(user.getBooks().stream()
                                     .filter(book -> !book.getId().equals(id))
                                     .collect(Collectors.toList()));
        userRepository.save(user);
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
        addressService.resetAddress(user);
        return userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public List<RentalHistory> getUserRentalHistory(Long id) throws NoRentalHistoryException, NoSuchUserException {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            List<RentalHistory> rentalHistory = userOptional.get().getRentalHistoryList();
            if (rentalHistory.isEmpty()) {
                throw new NoRentalHistoryException("This user hasn't rented any books yet,");
            }
            return rentalHistory;
        }
        throw new NoSuchUserException("A customer with that id does not exist.");
    }
}

