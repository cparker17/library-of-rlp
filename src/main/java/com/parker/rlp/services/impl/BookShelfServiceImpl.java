package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.book.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.models.books.BookShelf;
import com.parker.rlp.repositories.BookCaseRepository;
import com.parker.rlp.repositories.BookShelfRepository;
import com.parker.rlp.services.BookCaseService;
import com.parker.rlp.services.BookShelfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookShelfServiceImpl implements BookShelfService {
    @Autowired
    BookCaseService bookCaseService;

    @Autowired
    BookShelfRepository bookShelfRepository;

    @Autowired
    BookCaseRepository bookCaseRepository;

    public List<BookShelf> getShelvesWithTargetSubject(Book newBook) {
        List<BookShelf> bookShelvesWithSubject = new ArrayList<>();

        for (BookShelf bookShelf : bookShelfRepository.findAll()) {
            if (!bookShelf.isBottomShelf()) {
                for (Book book : bookShelf.getBooks()) {
                    if (book.getSubject().equals(newBook.getSubject())) {
                        bookShelvesWithSubject.add(bookShelf);
                        break;
                    }
                }
            }
        }
        return bookShelvesWithSubject;
    }

    public BookShelf getTargetBookShelf(List<BookShelf> bookShelvesWithSubject, Book newBook) {
        for (BookShelf bookShelf : bookShelvesWithSubject) {
            List<Book> books = bookShelf.getBooks();
            for (Book book : books) {
                if (book.getTitle().compareToIgnoreCase(newBook.getTitle()) > 0 &&
                        book.getBookNumber() != books.size() - 1) {
                    return bookShelf;
                }
                if (book.getTitle().compareToIgnoreCase(newBook.getTitle()) > 0 &&
                        book.getBookNumber() == books.size() - 1) {
                    bookShelvesWithSubject.remove(bookShelf);
                    return getTargetBookShelf(bookShelvesWithSubject, newBook);
                }
            }
        }
        return bookShelvesWithSubject.get(bookShelvesWithSubject.size() - 1);
    }

    public void addBookToBottomShelf(Book newBook, BookCase bookCase) throws NoSuchBookCaseException {
        if (bookCase.hasBottomShelf()) {
            addBook(newBook, bookCase.getBottomShelf(), bookCase);
            return;
        }
        for (BookCase availableBookCase : bookCaseRepository.findAll()) {
            if (availableBookCase.hasBottomShelf()) {
                addBook(newBook, availableBookCase.getBottomShelf(), availableBookCase);
                return;
            }
        }
    }

    public List<String> addBook(Book newBook, BookShelf bookShelf, BookCase bookCase) throws NoSuchBookCaseException {
        if (bookShelf.getOpenSpaceWidth() > newBook.getThickness()) {
            bookShelf.addBookToBookShelf(newBook);
            getBookPositionAndSetLocation(newBook, bookShelf, bookCase);
            return null;
        }
        List<Book> booksToBeShifted;
        if (isShiftLeft(bookShelf)) {
            booksToBeShifted = getBooksToBeShiftedLeft(bookShelf, newBook.getThickness());
        } else {
            booksToBeShifted = getBooksToBeShiftedRight(bookShelf, newBook.getThickness());
        }
        removeBooksToBeShifted(bookShelf, booksToBeShifted);
        bookShelf.addBookToBookShelf(newBook);
        getBookPositionAndSetLocation(newBook, bookShelf, bookCase);

        List<BookShelf> shelves = bookShelfRepository.findAll();
        shelves.removeIf(BookShelf::isBottomShelf);
        List<String> shiftDirections = new ArrayList<>();

        if (isShiftLeft(bookShelf)) {
            shelves.removeIf(bookShelf1 -> bookShelf1.getId() >= bookShelf.getId());
            shiftBooksLeft(booksToBeShifted, bookShelf, shelves, shiftDirections);
        } else {
            shelves.removeIf(bookShelf1 -> bookShelf1.getId() <= bookShelf.getId());
            shiftBooksRight(booksToBeShifted, shelves, shiftDirections);
        }
        Collections.reverse(shiftDirections);
        return shiftDirections;
    }

    private void removeBooksToBeShifted(BookShelf bookShelf, List<Book> booksToBeShifted) {
        bookShelf.getBooks().removeAll(booksToBeShifted);
        for (Book bookToBeShifted : booksToBeShifted) {
            bookShelf.updateOpenSpaceWidth((bookToBeShifted.getThickness() * -1));
        }
    }

    private List<Book> getBooksToBeShiftedLeft(BookShelf bookShelf, double bookThickness) {
        List<Book> booksToBeShifted = new ArrayList<>();

        for (Book book : bookShelf.getBooks()) {
            if (bookThickness > bookShelf.getOpenSpaceWidth()) {
                booksToBeShifted.add(book);
                bookThickness -= book.getThickness();
            }
        }
        return booksToBeShifted;
    }

    private List<Book> getBooksToBeShiftedRight(BookShelf bookShelf, double bookThickness) {
        List<Book> booksToBeShifted = new ArrayList<>();

        List<Book> books = bookShelf.getBooks();
        for (int i = books.size() - 1; i >= 0; i--) {
            if (bookThickness > bookShelf.getOpenSpaceWidth()) {
                booksToBeShifted.add(books.get(i));
                bookThickness -= books.get(i).getThickness();
            }
        }
        return booksToBeShifted;
    }

    private void shiftBooksLeft(List<Book> booksToBeShifted, BookShelf bookShelf, List<BookShelf> upperShelves,
                                List<String> shiftDirections) throws NoSuchBookCaseException {
        double totalBookThickness = 0;
        for (Book bookToBeShifted : booksToBeShifted) {
            totalBookThickness += bookToBeShifted.getThickness();
        }
        List<Book> booksToBeShiftedNext;

        for (int i = upperShelves.size() - 1; i >= 0; i--) {
            Optional<BookCase> bookCaseOptional = bookCaseRepository.findById(upperShelves.get(i).getBookCaseNumber());
            if (bookCaseOptional.isPresent()) {
                if (booksToBeShifted.size() > 1) {
                    shiftDirections.add("Take " + booksToBeShifted.size() + " books from the start of Bookcase #" +
                            bookShelf.getBookCaseNumber() + ", Shelf #" + bookShelf.getShelfLocation() +
                            " and move them to the end of the previous shelf.");
                } else {
                    shiftDirections.add("Take " + booksToBeShifted.size() + " book from the start of Bookcase #" +
                            bookShelf.getBookCaseNumber() + ", Shelf #" + bookShelf.getShelfLocation() +
                            " and move it to the end of the previous shelf.");
                }
                if (bookShelf.getOpenSpaceWidth() > totalBookThickness) {
                    for (Book book : booksToBeShifted) {
                        addBook(book, upperShelves.get(i), bookCaseOptional.get());
                    }
                    break;
                } else {
                    booksToBeShiftedNext = getBooksToBeShiftedLeft(upperShelves.get(i), totalBookThickness);
                    removeBooksToBeShifted(bookShelf, booksToBeShifted);
                    for (Book book : booksToBeShifted) {
                        addBook(book, upperShelves.get(i), bookCaseOptional.get());
                    }
                    booksToBeShifted = booksToBeShiftedNext;
                    totalBookThickness = 0;
                    for (Book bookToBeShifted : booksToBeShifted) {
                        totalBookThickness += bookToBeShifted.getThickness();
                    }
                }
            }
        }
    }

    private void shiftBooksRight(List<Book> booksToBeShifted, List<BookShelf> upperShelves,
                                 List<String> shiftDirections) throws NoSuchBookCaseException {
        double totalBookThickness = 0;
        for (Book bookToBeShifted : booksToBeShifted) {
            totalBookThickness += bookToBeShifted.getThickness();
        }
        List<Book> booksToBeShiftedNext;

        for (BookShelf bookShelf : upperShelves) {
            Optional<BookCase> bookCaseOptional = bookCaseRepository.findById(bookShelf.getBookCaseNumber());
            if (bookCaseOptional.isPresent()) {
                if (booksToBeShifted.size() > 1) {
                    shiftDirections.add("Take " + booksToBeShifted.size() + " books from the end of Bookcase #" +
                            bookShelf.getBookCaseNumber() + ", Shelf #" + bookShelf.getShelfLocation() +
                            " and move them to the start of the next shelf.");
                } else {
                    shiftDirections.add("Take " + booksToBeShifted.size() + " book from the end of Bookcase #" +
                            bookShelf.getBookCaseNumber() + ", Shelf #" + bookShelf.getShelfLocation() +
                            " and move it to the start of the next shelf.");
                }
                if (bookShelf.getOpenSpaceWidth() > totalBookThickness) {
                    for (Book book : booksToBeShifted) {
                        addBook(book, bookShelf, bookCaseOptional.get());
                    }
                    break;
                } else {
                    booksToBeShiftedNext = getBooksToBeShiftedRight(bookShelf, totalBookThickness);
                    removeBooksToBeShifted(bookShelf, booksToBeShifted);
                    for (Book book : booksToBeShifted) {
                        addBook(book, bookShelf, bookCaseOptional.get());
                    }
                    booksToBeShifted = booksToBeShiftedNext;
                    totalBookThickness = 0;
                    for (Book bookToBeShifted : booksToBeShifted) {
                        totalBookThickness += bookToBeShifted.getThickness();
                    }
                }
            }
        }
    }

    private void getBookPositionAndSetLocation(Book newBook, BookShelf bookShelf, BookCase bookCase) {
        bookShelf.getBooks().sort(Comparator.comparing(Book::getSubject, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        bookShelfRepository.save(bookShelf);

        int count = 1;
        for (Book book : bookShelf.getBooks()) {
            if (book.getTitle().equals(newBook.getTitle())) {
                bookCaseService.setBookLocation(count, newBook, bookShelf, bookCase);
                return;
            }
            count++;
        }
    }

    private boolean isShiftLeft(BookShelf targetBookShelf) {
        double previousOpenSpaceWidth = 0;
        double afterOpenSpaceWidth = 0;
        for (BookShelf bookShelf : bookShelfRepository.findAll()) {
            if (!bookShelf.isBottomShelf()) {
                if (bookShelf.getId() < targetBookShelf.getId()) {
                    previousOpenSpaceWidth += bookShelf.getOpenSpaceWidth();
                } else if (bookShelf.getId() > targetBookShelf.getId()) {
                    afterOpenSpaceWidth += bookShelf.getOpenSpaceWidth();
                }
            }
        }

        return previousOpenSpaceWidth > afterOpenSpaceWidth;
    }
}
