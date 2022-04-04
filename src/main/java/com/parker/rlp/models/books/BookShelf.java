package com.parker.rlp.models.books;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BookShelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Book> books;

    private Long bookCaseNumber;
    private Integer shelfLocation;
    private Double shelfWidth;
    private Double openSpaceWidth;
    private boolean isBottomShelf;

    public void addBookToBookShelf(Book book, double bookThickness) {
        if (books == null) {
            books = new ArrayList<>();
        }
        books.add(book);
        updateOpenSpaceWidth(bookThickness);
    }

    public void updateOpenSpaceWidth(double bookThickness) {
        openSpaceWidth -= bookThickness;
    }

    public void setIsBottomShelf(boolean value) {
        isBottomShelf = value;
    }

    public boolean isBottomShelf() {
        return isBottomShelf;
    }
}
