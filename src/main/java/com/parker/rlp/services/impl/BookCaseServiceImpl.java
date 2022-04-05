package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.models.books.BookShelf;
import com.parker.rlp.repositories.BookCaseRepository;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.repositories.BookShelfRepository;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BookCaseServiceImpl implements BookCaseService {
    @Autowired
    BookShelfService bookShelfService;

    @Autowired
    BookCaseRepository bookCaseRepository;

    @Autowired
    BookShelfRepository bookShelfRepository;

    @Autowired
    BookRepository bookRepository;

    @Override
    public List<BookCase> getAllBooks() {
        return bookCaseRepository.findAll();
    }

    @Override
    public void addBookCase(BookCase bookCase) {
        List<BookShelf> shelvesToPersist = new ArrayList<>();
        for (int i = 0; i < bookCase.getNumberOfUpperShelves(); i++) {
            BookShelf upperShelf = new BookShelf();
            upperShelf.setShelfLocation(i + 1);
            upperShelf.setShelfWidth(bookCase.getBookCaseWidth());
            upperShelf.setOpenSpaceWidth(bookCase.getBookCaseWidth() * bookCase.getAvailableSpace() / 100);
            upperShelf.setAvailableSpace(bookCase.getAvailableSpace());
            shelvesToPersist.add(upperShelf);
            bookCase.addShelfToBookCase(upperShelf);
        }

        if (bookCase.hasBottomShelf()) {
            BookShelf bottomShelf = new BookShelf();
            bottomShelf.setShelfLocation(bookCase.getNumberOfUpperShelves() + 1);
            bottomShelf.setShelfWidth(bookCase.getBookCaseWidth());
            bottomShelf.setOpenSpaceWidth(bookCase.getBookCaseWidth() * bookCase.getAvailableSpace() / 100);
            bottomShelf.setAvailableSpace(bookCase.getAvailableSpace());
            bookCase.setBottomShelf(bottomShelf);
            bottomShelf.setIsBottomShelf(true);
            shelvesToPersist.add(bottomShelf);
        }

        bookShelfRepository.saveAll(shelvesToPersist);
        bookCase = bookCaseRepository.save(bookCase);
        for (BookShelf bookShelf : shelvesToPersist) {
            bookShelf.setBookCaseNumber(bookCase.getId());
        }
        bookShelfRepository.saveAll(shelvesToPersist);
    }

    @Override
    @Transactional
    public void loadBookCases() throws NoSuchBookCaseException {
        List<BookCase> bookCases = bookCaseRepository.findAll();
        List<Book> allBooks = bookRepository.findAll();

        allBooks.sort(Comparator.comparing(Book::getSubject, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));

        BookCase bookCase;
        BookShelf bookShelf;
        BookShelf bottomShelf = new BookShelf();
        List<BookShelf> bookShelves;
        List<Book> miscOverSizedBooks = new ArrayList<>();

        if (bookCases.isEmpty()) {
            throw new NoSuchBookCaseException("No bookcases created yet.  Please return to your dashboard to add a " +
                    "bookcase before loading the books.");
        }
        bookCases.sort(Comparator.comparing(BookCase::getId));
        bookCase = bookCases.get(0);
        bookShelves = bookCase.getUpperShelves();
        bookShelf = bookShelves.get(0);
        if (bookCase.hasBottomShelf()) {
            bottomShelf = bookCase.getBottomShelf();
        }

        List<BookShelf> shelvesToPersist = new ArrayList<>();
        shelvesToPersist.add(bottomShelf);
        shelvesToPersist.add(bookShelf);

        int totalBookCaseCount = bookCases.size();
        int caseCount = 0;
        int shelfCount = 0;
        int bookCount = 1;

        for (Book book : allBooks) {
            if (book.getHeight() > bookCase.getMaxBookHeight() || book.getDepth() > bookCase.getMaxBookDepth()) {
                if (bookCase.hasBottomShelf()) {
                    if (!miscOverSizedBooks.isEmpty()) {
                        for (Book overSizedBook : miscOverSizedBooks) {
                            bottomShelf.addBookToBookShelf(overSizedBook);
                            setBookLocation(bookCount++, overSizedBook, bottomShelf, bookCase);
                        }
                        miscOverSizedBooks = new ArrayList<>();
                    }
                    bottomShelf.addBookToBookShelf(book);
                    setBookLocation(bookCount++, book, bottomShelf, bookCase);
                } else {
                    miscOverSizedBooks.add(book);
                }
                continue;
            }
            if (book.getThickness() > bookShelf.getOpenSpaceWidth()) {
                bookCount = 1;
                if (++shelfCount < bookCase.getNumberOfUpperShelves()) {
                    shelvesToPersist.add(bookShelf);
                    bookShelf = bookShelves.get(shelfCount);
                } else {
                    if (++caseCount < totalBookCaseCount) {
                        shelvesToPersist.add(bottomShelf);
                        bookCase = bookCases.get(caseCount);
                        bookShelves = bookCase.getUpperShelves();
                        shelvesToPersist.add(bookShelf);
                        bookShelf = bookShelves.get(0);
                        if (bookCase.hasBottomShelf()) {
                            bottomShelf = bookCase.getBottomShelf();
                        }
                        shelfCount = 0;
                    } else {
                        throw new NoSuchBookCaseException("You are out of bookcase space.  Please return to your" +
                                "dashboard to add a new bookcase.");
                    }
                }
            }
            bookShelf.addBookToBookShelf(book);
            setBookLocation(bookCount++, book, bookShelf, bookCase);
        }
        setOpenSpaceWidthAfterLoading(shelvesToPersist);
    }

    private void setOpenSpaceWidthAfterLoading(List<BookShelf> bookShelves) {
        for (BookShelf shelf : bookShelves) {
            shelf.setOpenSpaceWidth(shelf.getOpenSpaceWidth() +
                    ((100 - shelf.getAvailableSpace()) / 100) * shelf.getShelfWidth());
        }
        bookShelfRepository.saveAll(bookShelves);
    }

    public void setBookLocation(int bookNumber, Book book, BookShelf bookShelf, BookCase bookCase) {
        book.setBookNumber(bookNumber);
        book.setBookShelfNumber(bookShelf.getShelfLocation());
        book.setBookCaseNumber(bookCase.getId());
    }

    @Transactional
    public List<String> addBookToBookCase(Book book) throws NoSuchBookCaseException {
        List<BookShelf> bookShelvesWithSubject = bookShelfService.getShelvesWithTargetSubject(book);
        BookShelf targetBookShelf = bookShelfService.getTargetBookShelf(bookShelvesWithSubject, book);

        for (BookCase bookCase : bookCaseRepository.findAll()) {
            for (BookShelf bookShelf : bookCase.getUpperShelves()) {
                if (bookShelf.getId().equals(targetBookShelf.getId())) {
                    if (book.getHeight() > bookCase.getMaxBookHeight() || book.getDepth() > bookCase.getMaxBookDepth()) {
                        bookShelfService.addBookToBottomShelf(book, bookCase);
                    } else {
                        List<String> shiftDirections = bookShelfService.addBook(book, targetBookShelf, bookCase);
                        return shiftDirections;
                    }
                }
            }
        }
        return null;
    }
}
