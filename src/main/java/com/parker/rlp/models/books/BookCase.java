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
public class BookCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private List<BookShelf> upperShelves;

    @ManyToOne
    private BookShelf bottomShelf;

    private Double bookCaseWidth;
    private Double maxBookHeight;
    private Double maxBookDepth;

    private boolean hasBottomShelf;

    private Integer numberOfUpperShelves;

    private Double availableSpace;

    public void addShelfToBookCase (BookShelf bookShelf) {
        if (upperShelves == null) {
            upperShelves = new ArrayList<>();
        }
        upperShelves.add(bookShelf);
    }

    public boolean hasBottomShelf () {
        return hasBottomShelf;
    }
}
