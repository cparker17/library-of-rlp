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

    @OneToMany(cascade = CascadeType.ALL)
    private List<Book> books;

    private Long bookCaseNumber;
    private Integer shelfLocation;
    private Double shelfWidth;
    private Double openSpaceWidth;
    private Double availableSpace;
    private boolean isBottomShelf;

    public void addBookToBookShelf(Book book) {
        if (books == null) {
            books = new ArrayList<>();
        }
        books.add(book);
        updateOpenSpaceWidth(book.getThickness());
    }

    public void updateOpenSpaceWidth(double bookThickness) {
        openSpaceWidth -= bookThickness;
    }

    public boolean isBottomShelf() {
        return isBottomShelf;
    }
}
