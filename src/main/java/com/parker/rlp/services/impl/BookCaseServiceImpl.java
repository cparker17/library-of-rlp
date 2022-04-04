package com.parker.rlp.services.impl;

import com.parker.rlp.exceptions.NoSuchBookCaseException;
import com.parker.rlp.models.books.Book;
import com.parker.rlp.models.books.BookCase;
import com.parker.rlp.models.books.BookShelf;
import com.parker.rlp.repositories.BookCaseRepository;
import com.parker.rlp.repositories.BookRepository;
import com.parker.rlp.repositories.BookShelfRepository;
import com.parker.rlp.services.BookCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BookCaseServiceImpl implements BookCaseService {
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
            shelvesToPersist.add(upperShelf);
            bookCase.addShelfToBookCase(upperShelf);
        }

        if (bookCase.hasBottomShelf()) {
            BookShelf bottomShelf = new BookShelf();
            bottomShelf.setShelfLocation(bookCase.getNumberOfUpperShelves() + 1);
            bottomShelf.setShelfWidth(bookCase.getBookCaseWidth());
            bottomShelf.setOpenSpaceWidth(bookCase.getBookCaseWidth() * bookCase.getAvailableSpace() / 100);
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

        BookShelf bookShelf;
        BookShelf bottomShelf = new BookShelf();
        BookCase bookCase;
        int totalBookCaseCount;
        List<BookShelf> bookShelves;
        List<Book> miscOverSizedBooks = new ArrayList<>();

        if (bookCases.isEmpty()) {
            throw new NoSuchBookCaseException("No bookcases created yet.  Please return to your dashboard to add a " +
                    "bookcase before loading the books.");
        } else {
            bookCases.sort(Comparator.comparing(BookCase::getId));
            totalBookCaseCount = bookCases.size();
            bookCase = bookCases.get(0);
            bookShelves = bookCase.getUpperShelves();
            bookShelf = bookShelves.get(0);
            if (bookCase.hasBottomShelf()) {
                bottomShelf = bookCase.getBottomShelf();
            }
        }

        List<BookShelf> bottomShelvesToPersist = new ArrayList<>();
        int bookCount = 1;
        int shelfCount = 0;
        int caseCount = 0;

        for (Book book : allBooks) {
            if (book.getHeight() > bookCase.getMaxBookHeight() || book.getDepth() > bookCase.getMaxBookDepth()) {
                if (bookCase.hasBottomShelf()) {
                    if (!miscOverSizedBooks.isEmpty()) {
                        for (Book overSizedBook : miscOverSizedBooks) {
                            bottomShelf.addBookToBookShelf(overSizedBook, book.getThickness());
                            setBookLocation(bookCount++, overSizedBook, bottomShelf, bookCase);
                        }
                        miscOverSizedBooks = new ArrayList<>();
                    }
                    bottomShelf.addBookToBookShelf(book, book.getThickness());
                    setBookLocation(bookCount++, book, bottomShelf, bookCase);
                } else {
                    miscOverSizedBooks.add(book);
                }
                continue;
            }
            if (book.getThickness() > bookShelf.getOpenSpaceWidth()) {
                bookCount = 1;
                if (++shelfCount < bookCase.getNumberOfUpperShelves()) {
                    bookShelf = bookShelves.get(shelfCount);
                } else {
                    if (++caseCount < totalBookCaseCount) {
                        bottomShelvesToPersist.add(bottomShelf);
                        bookCase = bookCases.get(caseCount);
                        bookShelves = bookCase.getUpperShelves();
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
            bookShelf.addBookToBookShelf(book, book.getThickness());
            setBookLocation(bookCount++, book, bookShelf, bookCase);
        }

        bookShelfRepository.saveAll(bottomShelvesToPersist);
        bookRepository.saveAll(allBooks);
        setOpenSpaceWidthAfterLoading(bookCases, bookShelves);
    }

    private void setOpenSpaceWidthAfterLoading(List<BookCase> bookCases, List<BookShelf> bookShelves) {
        for (BookCase bookCase : bookCases) {
            for (BookShelf shelf : bookShelves) {
                shelf.setOpenSpaceWidth(shelf.getOpenSpaceWidth() +
                        ((100 - bookCase.getAvailableSpace()) / 100) * shelf.getShelfWidth());
            }
        }
        bookShelfRepository.saveAll(bookShelves);
        bookCaseRepository.saveAll(bookCases);
    }

    private void setBookLocation(int bookNumber, Book book, BookShelf bookShelf, BookCase bookCase) {
        book.setBookNumber(bookNumber);
        book.setBookShelfNumber(bookShelf.getShelfLocation());
        book.setBookCaseNumber(bookCase.getId());
    }

    @Transactional
    public String addBookToBookCase(Book newBook) throws NoSuchBookCaseException {
        List<BookShelf> bookShelvesWithSubject = getShelvesWithTargetSubject(newBook);
        BookShelf targetBookShelf = getTargetBookShelf(bookShelvesWithSubject, newBook);
        String shiftDirection;
        for (BookCase bookCase : bookCaseRepository.findAll()) {
            for (BookShelf bookShelf : bookCase.getUpperShelves()) {
                if (bookShelf.getId().equals(targetBookShelf.getId())) {
                    if (newBook.getHeight() > bookCase.getMaxBookHeight() || newBook.getDepth() >
                            bookCase.getMaxBookDepth()) {
                        addBookToBottomShelf(newBook, bookCase);
                    } else {
                        shiftDirection = addBook(newBook, targetBookShelf, bookCase);
                        targetBookShelf.updateOpenSpaceWidth(newBook.getThickness());
                        return shiftDirection;
                    }

                }
            }
        }
        return null;
    }

    private List<BookShelf> getShelvesWithTargetSubject(Book newBook) {
        boolean containsSubject = false;
        List<BookShelf> bookShelvesWithSubject = new ArrayList<>();
        for (BookShelf bookShelf : bookShelfRepository.findAll()) {
            if (!bookShelf.isBottomShelf()) {
                for (Book book : bookShelf.getBooks()) {
                    if (book.getSubject().equals(newBook.getSubject())) {
                        containsSubject = true;
                        break;
                    }
                }
                if (containsSubject) {
                    bookShelvesWithSubject.add(bookShelf);
                }
                containsSubject = false;
            }
        }
        return bookShelvesWithSubject;
    }

    private BookShelf getTargetBookShelf(List<BookShelf> bookShelvesWithSubject, Book newBook) {
        for (BookShelf bookShelf : bookShelvesWithSubject) {
            List<Book> books = bookShelf.getBooks();
            for (Book book : books) {
                if (book.getTitle().compareToIgnoreCase(newBook.getTitle()) > 0 &&
                        book.getBookNumber() != books.size() - 1) {
                    return bookShelf;
                } else if (book.getTitle().compareToIgnoreCase(newBook.getTitle()) > 0 &&
                        book.getBookNumber() == books.size() - 1) {
                    bookShelvesWithSubject.remove(bookShelf);
                    return getTargetBookShelf(bookShelvesWithSubject, newBook);
                }
            }
        }
        return bookShelvesWithSubject.get(bookShelvesWithSubject.size() - 1);
    }

    private void addBookToBottomShelf(Book newBook, BookCase bookCase) throws NoSuchBookCaseException {
        if (bookCase.hasBottomShelf()) {
            addBook(newBook, bookCase.getBottomShelf(), bookCase);
            bookCase.getBottomShelf().updateOpenSpaceWidth(newBook.getThickness());
        } else {
            for (BookCase availableBookCase : bookCaseRepository.findAll()) {
                if (availableBookCase.hasBottomShelf()) {
                    addBook(newBook, availableBookCase.getBottomShelf(), availableBookCase);
                    availableBookCase.getBottomShelf().updateOpenSpaceWidth(newBook.getThickness());
                }
            }
        }
    }

    private String addBook(Book newBook, BookShelf bookShelf, BookCase bookCase) throws NoSuchBookCaseException {
        if (bookShelf.getOpenSpaceWidth() > newBook.getThickness()) {
            bookShelf.addBookToBookShelf(newBook, newBook.getThickness());
            getBookPositionAndSetLocation(newBook, bookShelf, bookCase);
        } else {
            List<Book> booksToBeShifted;
            if (isShiftLeft(bookShelf)) {
                booksToBeShifted = getBooksToBeShiftedLeft(bookShelf, newBook.getThickness());
            } else {
                booksToBeShifted = getBooksToBeShiftedRight(bookShelf, newBook.getThickness());
            }
            bookShelf.getBooks().removeAll(booksToBeShifted);
            for (Book bookToBeShifted : booksToBeShifted) {
                bookShelf.updateOpenSpaceWidth((bookToBeShifted.getThickness() * -1));
            }
            bookShelf.addBookToBookShelf(newBook, newBook.getThickness());
            getBookPositionAndSetLocation(newBook, bookShelf, bookCase);

            List<BookShelf> shelves = bookShelfRepository.findAll();
            shelves.removeIf(BookShelf::isBottomShelf);
            List<String> shiftDirections = new ArrayList<>();

            if (isShiftLeft(bookShelf)) {
                shelves.removeIf(bookShelf1 -> bookShelf1.getId() >= bookShelf.getId());
                shiftBooksLeft(booksToBeShifted, bookShelf, shelves, shiftDirections);
                return "left";
            } else {
                shelves.removeIf(bookShelf1 -> bookShelf1.getId() <= bookShelf.getId());
                shiftBooksRight(booksToBeShifted, shelves, shiftDirections);
                return "right";
            }
        }

        return null;
    }

    private List<Book> getBooksToBeShiftedLeft(BookShelf bookShelf, double bookThickness) {
        List<Book> booksToBeShifted = new ArrayList<>();
        double totalBookThickness = bookThickness;
        for (Book book : bookShelf.getBooks()) {
            if (totalBookThickness > bookShelf.getOpenSpaceWidth()) {
                booksToBeShifted.add(book);
                totalBookThickness -= book.getThickness();
            }
        }
        return booksToBeShifted;
    }

    private List<Book> getBooksToBeShiftedRight(BookShelf bookShelf, double bookThickness) {
        List<Book> booksToBeShifted = new ArrayList<>();
        double totalBookThickness = bookThickness;
        List<Book> books = bookShelf.getBooks();
        for (int i = books.size() - 1; i >= 0; i--) {
            if (totalBookThickness > bookShelf.getOpenSpaceWidth()) {
                booksToBeShifted.add(books.get(i));
                totalBookThickness -= books.get(i).getThickness();
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
        Optional<BookCase> bookCaseOptional;

        for (int i = upperShelves.size() - 1; i >= 0; i--) {
            bookCaseOptional = bookCaseRepository.findById(upperShelves.get(i).getBookCaseNumber());
            if (bookCaseOptional.isPresent()) {
                if (bookShelf.getOpenSpaceWidth() > totalBookThickness) {
                    for (Book book : booksToBeShifted) {
                        addBook(book, upperShelves.get(i), bookCaseOptional.get());
                    }
                    break;
                } else {
                    booksToBeShiftedNext = getBooksToBeShiftedLeft(upperShelves.get(i), totalBookThickness);
                    upperShelves.get(i).getBooks().removeAll(booksToBeShiftedNext);
                    for (Book book : booksToBeShiftedNext) {
                        bookShelf.updateOpenSpaceWidth(book.getThickness() * -1);
                    }
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
        Optional<BookCase> bookCaseOptional;
        for (BookShelf bookShelf : upperShelves) {
            bookCaseOptional = bookCaseRepository.findById(bookShelf.getBookCaseNumber());
            if (bookCaseOptional.isPresent()) {
                shiftDirections.add("Take " + booksToBeShifted.size() + " books from Bookcase #" +
                        bookShelf.getBookCaseNumber() + ", Shelf #" + bookShelf.getShelfLocation() +
                        " and move them to the start of the next ")
                if (bookShelf.getOpenSpaceWidth() > totalBookThickness) {
                    for (Book book : booksToBeShifted) {
                        addBook(book, bookShelf, bookCaseOptional.get());
                    }
                    break;
                } else {
                    booksToBeShiftedNext = getBooksToBeShiftedRight(bookShelf, totalBookThickness);
                    bookShelf.getBooks().removeAll(booksToBeShiftedNext);
                    for (Book book : booksToBeShiftedNext) {
                        bookShelf.updateOpenSpaceWidth(book.getThickness() * -1);
                    }
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
        bookCaseRepository.save(bookCase);

        int count = 1;
        for (Book book : bookShelf.getBooks()) {
            if (book.getTitle().equals(newBook.getTitle())) {
                setBookLocation(count, newBook, bookShelf, bookCase);
                break;
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
