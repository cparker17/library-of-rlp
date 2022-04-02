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
        for (int i = 0; i < bookCase.getNumberOfUpperShelves(); i++) {
            BookShelf upperShelf = new BookShelf();
            upperShelf.setShelfLocation(i + 1);
            upperShelf.setShelfWidth(bookCase.getBookCaseWidth());
            upperShelf.setOpenSpaceWidth(bookCase.getBookCaseWidth() * bookCase.getAvailableSpace() / 100);
            bookShelfRepository.save(upperShelf);
            bookCase.addShelfToBookCase(upperShelf);
        }

        BookShelf bottomShelf = new BookShelf();
        bottomShelf.setShelfLocation(bookCase.getNumberOfUpperShelves() + 1);
        bottomShelf.setShelfWidth(bookCase.getBookCaseWidth());
        bottomShelf.setOpenSpaceWidth(bookCase.getBookCaseWidth() * bookCase.getAvailableSpace() / 100);
        bookCase.setBottomShelf(bottomShelf);
        bottomShelf.setIsBottomShelf(true);
        bookShelfRepository.save(bottomShelf);

        bookCaseRepository.save(bookCase);
    }

    @Override
    @Transactional
    public void loadBookCases() throws NoSuchBookCaseException {
        List<BookCase> bookCases = bookCaseRepository.findAll();
        List<Book> allBooks = bookRepository.findAll();

        allBooks.sort(Comparator.comparing(Book::getSubject, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));

        BookShelf bookShelf;
        BookShelf bottomShelf;
        BookCase bookCase;
        int totalBookCaseCount;
        List<BookShelf> bookShelves;

        if (bookCases.isEmpty()) {
            throw new NoSuchBookCaseException("No bookcases created yet.  Please return to your dashboard to add a " +
                    "bookcase before loading the books.");
        } else {
            bookCases.sort(Comparator.comparing(BookCase::getId));
            totalBookCaseCount = bookCases.size();
            bookCase = bookCases.get(0);
            bookShelves = bookCase.getUpperShelves();
            bookShelf = bookShelves.get(0);
            bottomShelf = bookCase.getBottomShelf();
        }

        int bookCount = 1;
        int shelfCount = 0;
        int caseCount = 0;

        for (Book book : allBooks) {
            if (book.getHeight() > 300 || book.getDepth() > 300) {
                bottomShelf.addBookToBookShelf(book);
                bottomShelf.updateOpenSpaceWidth(book.getThickness());
                setBookLocation(bookCount++, book, bottomShelf, bookCase);
                continue;
            }
            if (book.getThickness() > bookShelf.getOpenSpaceWidth()) {
                bookCount = 1;
                if (++shelfCount < bookCase.getNumberOfUpperShelves()) {
                    bookShelf = bookShelves.get(shelfCount);
                } else {
                    if (++caseCount < totalBookCaseCount) {
                        bookCase = bookCases.get(caseCount);
                        bookShelves = bookCase.getUpperShelves();
                        bookShelf = bookShelves.get(0);
                        bottomShelf = bookCase.getBottomShelf();
                        shelfCount = 0;
                    } else {
                        throw new NoSuchBookCaseException("You are out of bookcase space.  Please return to your" +
                                "dashboard to add a new bookcase.");
                    }
                }
            }
            bookShelf.addBookToBookShelf(book);
            setBookLocation(bookCount++, book, bookShelf, bookCase);
            bookShelf.updateOpenSpaceWidth(book.getThickness());
        }

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
    public void addBookToBookCase(Book newBook) throws NoSuchBookCaseException {
        List<BookShelf> bookShelvesWithSubject = getShelvesWithTargetSubject(newBook);
        BookShelf targetBookShelf = getTargetBookShelf(bookShelvesWithSubject, newBook);
        for (BookCase bookCase : bookCaseRepository.findAll()) {
            for (BookShelf bookShelf : bookCase.getUpperShelves()) {
                if (bookShelf.getId().equals(targetBookShelf.getId())) {
                    addBook(newBook, targetBookShelf, bookCase);
                    targetBookShelf.updateOpenSpaceWidth(newBook.getThickness());
                    return;
                }
            }
        }
    }

    private List<BookShelf> getShelvesWithTargetSubject(Book newBook) {
        boolean containsSubject = false;
        List<BookShelf> bookShelvesWithSubject = new ArrayList<>();
        for (BookShelf bookShelf : bookShelfRepository.findAll()) {
            if (!bookShelf.isBottomShelf()) {
                for (Book book : bookShelf.getBooks()) {
                    if (book.getSubject().equals(newBook.getSubject())) {
                        containsSubject = true;
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
                if (book.getTitle().compareToIgnoreCase(newBook.getTitle()) > 0) {
                    if (book.getBookNumber() == books.size()) {
                    } else {
                        return bookShelf;
                    }
                }
            }
        }
        return bookShelvesWithSubject.get(bookShelvesWithSubject.size() - 1);
    }

    private void addBook(Book newBook, BookShelf bookShelf, BookCase bookCase) {
        if (bookShelf.getOpenSpaceWidth() > newBook.getThickness()) {
            bookShelf.addBookToBookShelf(newBook);
            bookShelf.updateOpenSpaceWidth(newBook.getThickness());
        } else {
            BookShelf openBookShelf = getPreviousOpenShelf(bookShelf, newBook);
            if (openBookShelf != null) {
                openBookShelf.updateOpenSpaceWidth(newBook.getThickness());
                addBook(newBook, openBookShelf, bookCase);
            } else {
                openBookShelf = getNextOpenShelf(bookShelf, newBook);
                if (openBookShelf != null) {
                    openBookShelf.updateOpenSpaceWidth(newBook.getThickness());
                    addBook(newBook, openBookShelf, bookCase);
                }
            }
        }

        bookShelf.getBooks().sort(Comparator.comparing(Book::getSubject).thenComparing(Book::getTitle));
        bookShelfRepository.save(bookShelf);
        bookCaseRepository.save(bookCase);

        int count = 1;
        for (Book book : bookShelf.getBooks()) {
            if (book.getTitle().equals(newBook.getTitle())) {
                setBookLocation(count, newBook, bookShelf, bookCase);
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

    private BookShelf getPreviousOpenShelf(BookShelf bookShelf, Book newBook) {
        Optional<BookShelf> previousShelfOptional = bookShelfRepository.findById(bookShelf.getId() - 1);
        if (previousShelfOptional.isPresent()) {
            BookShelf previousBookShelf = previousShelfOptional.get();
            if (previousBookShelf.isBottomShelf()) {
                previousShelfOptional = bookShelfRepository.findById(bookShelf.getId() - 2);
                if (previousShelfOptional.isPresent()) {
                    previousBookShelf = previousShelfOptional.get();
                }
            }
            if (previousBookShelf.getOpenSpaceWidth() > newBook.getThickness()) {
                return previousBookShelf;
            } else {
                BookShelf previousOpenShelf = getPreviousOpenShelf(previousBookShelf, newBook);
                if (previousOpenShelf == null) {
                    return null;
                } else if (previousOpenShelf.getOpenSpaceWidth() > newBook.getThickness()) {
                    return previousOpenShelf;
                }
            }
        }
        return null;
    }

    private BookShelf getNextOpenShelf(BookShelf bookShelf, Book newBook) {
        Optional<BookShelf> nextShelfOptional = bookShelfRepository.findById(bookShelf.getId() + 1);
        if (nextShelfOptional.isPresent()) {
            BookShelf nextBookShelf = nextShelfOptional.get();
            if (nextBookShelf.isBottomShelf()) {
                nextShelfOptional = bookShelfRepository.findById(bookShelf.getId() + 2);
                if (nextShelfOptional.isPresent()) {
                    nextBookShelf = nextShelfOptional.get();
                }
            }
            if (nextBookShelf.getOpenSpaceWidth() > newBook.getThickness()) {
                return nextBookShelf;
            } else {
                BookShelf nextOpenShelf = getNextOpenShelf(nextBookShelf, newBook);
                if (nextOpenShelf == null) {
                    return null;
                } else if (nextOpenShelf.getOpenSpaceWidth() > newBook.getThickness()) {
                    return nextOpenShelf;
                }
            }
        }
        return null;
    }

    private String getShiftDirection(Long bookCaseId, Integer bookShelfLocation) {
        double beforeAvailableWidth = getBeforeAvailableWidth(bookCaseId, bookShelfLocation);
        double afterAvailableWidth = getAfterAvailableWidth(beforeAvailableWidth);
        if (beforeAvailableWidth > afterAvailableWidth) {
            return "Shift all books to the left.";
        } else {
            return "Shift all books to the right.";
        }
    }

    private double getBeforeAvailableWidth(Long bookCaseId, Integer bookShelfLocation) {
        double beforeAvailableWidth = 0;
        for (BookCase bookCase : bookCaseRepository.findAll()) {
            for (BookShelf bookShelf : bookCase.getUpperShelves()) {
                if (bookCase.getId().equals(bookCaseId) && bookShelf.getShelfLocation().equals(bookShelfLocation)) {
                    return beforeAvailableWidth;
                } else {
                    beforeAvailableWidth += bookShelf.getOpenSpaceWidth();
                }
            }
        }
        return beforeAvailableWidth;
    }

    private double getAfterAvailableWidth(double beforeAvailableWidth) {
        double afterAvailableWidth = 0;
        for (BookCase bookCase : bookCaseRepository.findAll()) {
            for (BookShelf bookShelf : bookCase.getUpperShelves()) {
                afterAvailableWidth += bookShelf.getOpenSpaceWidth();
            }
        }
        return afterAvailableWidth - beforeAvailableWidth;
    }
}
