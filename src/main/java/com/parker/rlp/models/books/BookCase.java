package com.parker.rlp.models.books;

import com.sun.istack.NotNull;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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

    @OneToMany
    private List<BookShelf> upperShelves;

    @OneToOne
    private BookShelf bottomShelf;

    private Double bookCaseWidth;
    private Double maxBookHeight;
    private Double maxBookDepth;
    private Integer numberOfUpperShelves;
    private Double availableSpace;

    private boolean hasBottomShelf;

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
