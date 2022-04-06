package com.parker.rlp.models.books;

import com.parker.rlp.models.users.User;
import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "books")
@Builder
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @NotBlank(message = "Title required.")
    private String title;

    @ManyToOne
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Subject subject;

    @NotNull
    @NotBlank(message = "Author required.")
    private String author;

    @NotNull
    @NotBlank(message = "ISBN required.")
    private String isbn;


    private Double height;
    private Double depth;
    private Double thickness;

    private String imageFile;
    private LocalDate dateAdded;
    private LocalDate dateRented;
    private LocalDate dateDue;
    private LocalDate dateReturned;
    private Long bookCaseNumber;
    private Integer bookShelfNumber;
    private Integer bookNumber;

    private Boolean isCheckedOut;

    @ManyToOne
    private User user;

    @OneToMany
    @JoinColumn(name = "book_id")
    private List<BookHistory> bookHistoryList;

    public void addRentalHistory(BookHistory rentalHistory) {
        if (bookHistoryList == null) {
            bookHistoryList = new ArrayList<>();
        } else {
            bookHistoryList.add(rentalHistory);
        }
    }

    public void setTitle(String title) {
        if (title.toUpperCase().startsWith("A")) {
            String[] modifiedTitle = title.split(" ", 2);
            this.title = modifiedTitle[1].concat(", A");
        } else if (title.toUpperCase().startsWith("THE")) {
            String[] modifiedTitle = title.split(" ", 2);
            this.title = modifiedTitle[1].concat(", The");
        } else {
            this.title = title;
        }
    }
}
